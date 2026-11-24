/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.MeetingDaoHelper;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

/**
 * LIVE end-to-end integration test for KE-3527 (timezone shift on Schedule Meeting).
 *
 * <p>Unlike {@code MeetingDaoHelperTest} (pure unit, no network), this test actually SCHEDULES real events
 * on a live Microsoft 365 calendar via Microsoft Graph, using the extension's own fixed
 * {@link MeetingDaoHelper#buildDateTimeTimeZone(Long, String, String)} to construct the payload — then reads
 * each event back and asserts the stored absolute instant equals the epoch that was passed in. That is the
 * definitive proof of the ticket's acceptance criteria: a shift scheduled at a given local time is stored at
 * exactly that instant and therefore displays as the correct local time to a viewer in the event's zone.
 *
 * <p>It is DISABLED by default (JUnit {@code Assume}) so CI never needs Graph credentials. To run it:
 * <pre>
 *   GRAPH_ACCESS_TOKEN=&lt;a delegated Graph token with Calendars.ReadWrite&gt; \
 *     ./gradlew :office_365_calendar:test --tests "*MeetingSchedulingLiveIntegrationTest"
 * </pre>
 * (Get a token quickly from Graph Explorer -> Access token, or your own OAuth flow. The token's user is the
 * mailbox the events are created in; every event created here is deleted again at the end of its test.)
 *
 * <p>Each case builds the payload with {@code clientTimeZone = "UTC"} on purpose: that is the production
 * misconfiguration (extension default = UTC, event zone = America/*) that used to trigger the bug. With the
 * fix, the instant still round-trips exactly.
 */
public class MeetingSchedulingLiveIntegrationTest {

    private static final String EFFECTIVE_ZONE = "UTC"; // extension default that differs from the event zone
    private static final DateTimeFormatter GRAPH_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static GraphServiceClient<Request> graph;

    @BeforeClass
    public static void setUp() {
        String token = System.getenv("GRAPH_ACCESS_TOKEN");
        if (token == null || token.isBlank()) {
            token = System.getProperty("graph.access.token");
        }
        Assume.assumeTrue(
                "Skipping live Graph test: set GRAPH_ACCESS_TOKEN (or -Dgraph.access.token) to run it.",
                token != null && !token.isBlank());
        final String bearer = token.trim();
        IAuthenticationProvider auth = (URL requestUrl) -> CompletableFuture.completedFuture(bearer);
        graph = GraphServiceClient.builder().authenticationProvider(auth).buildClient();
    }

    /** Ticket AC2 — Hudson, FL: 8:00 AM - 5:30 PM Eastern (DST). Exact epochs from the ticket. */
    @Test
    public void schedulesEasternDaylightShiftAtCorrectInstant() {
        assertScheduledInstant("LIVE-IT Eastern 8:00-5:30 (DST)",
                1790164800000L, 1790199000000L, "America/New_York",
                "2026-09-23T08:00:00", "2026-09-23T17:30:00");
    }

    /** Ticket AC1 — 9:00 AM - 5:00 PM Central (DST). */
    @Test
    public void schedulesCentralDaylightShiftAtCorrectInstant() {
        assertScheduledInstant("LIVE-IT Central 9:00-5:00 (DST)",
                1790172000000L, 1790200800000L, "America/Chicago",
                "2026-09-23T09:00:00", "2026-09-23T17:00:00");
    }

    /** Standard-time (winter) case — 9:00 AM - 5:00 PM Central (CST). */
    @Test
    public void schedulesCentralStandardShiftAtCorrectInstant() {
        assertScheduledInstant("LIVE-IT Central 9:00-5:00 (STD)",
                1797865200000L, 1797894000000L, "America/Chicago",
                "2026-12-21T09:00:00", "2026-12-21T17:00:00");
    }

    /**
     * Builds the event exactly as the fixed extension does, POSTs it, reads it back both in UTC (to check the
     * absolute instant) and in the event zone (to check the human-visible wall-clock), then deletes it.
     */
    private void assertScheduledInstant(String subject, long fromEpoch, long toEpoch, String eventZone,
                                        String expectedLocalStart, String expectedLocalEnd) {
        Event created = null;
        try {
            Event event = new Event();
            event.subject = subject;
            event.start = MeetingDaoHelper.buildDateTimeTimeZone(fromEpoch, eventZone, EFFECTIVE_ZONE);
            event.end = MeetingDaoHelper.buildDateTimeTimeZone(toEpoch, eventZone, EFFECTIVE_ZONE);

            created = graph.me().events().buildRequest().post(event);

            // 1) Absolute instant must be preserved — read back in UTC and compare epoch-for-epoch.
            Event inUtc = graph.me().events(created.id).buildRequest(preferZone("UTC")).get();
            assertEquals(subject + " start instant", fromEpoch, toEpochMillis(inUtc.start, "UTC"));
            assertEquals(subject + " end instant", toEpoch, toEpochMillis(inUtc.end, "UTC"));

            // 2) Human-visible time in the event's own zone must be the intended local wall-clock.
            Event inZone = graph.me().events(created.id).buildRequest(preferZone(eventZone)).get();
            assertEquals(subject + " start local", expectedLocalStart, trimGraphDateTime(inZone.start.dateTime));
            assertEquals(subject + " end local", expectedLocalEnd, trimGraphDateTime(inZone.end.dateTime));
        } finally {
            if (created != null && created.id != null) {
                graph.me().events(created.id).buildRequest().delete();
            }
        }
    }

    private static List<Option> preferZone(String zone) {
        List<Option> options = new ArrayList<>();
        options.add(new HeaderOption("Prefer", "outlook.timezone=\"" + zone + "\""));
        return options;
    }

    /** Graph returns dateTime like "2026-09-23T08:00:00.0000000"; parse the seconds-precision prefix. */
    private static String trimGraphDateTime(String graphDateTime) {
        return graphDateTime.substring(0, 19);
    }

    private static long toEpochMillis(DateTimeTimeZone dtz, String zone) {
        LocalDateTime local = LocalDateTime.parse(trimGraphDateTime(dtz.dateTime), GRAPH_LOCAL);
        ZonedDateTime zoned = local.atZone(ZoneId.of(zone));
        return zoned.toInstant().toEpochMilli();
    }
}
