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

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entityrequests.impl;

import app.krista.extension.executor.QueryClause;
import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EventDetailsImplTest {

    @Mock private MeetingDaoImpl meetingDao;
    @Mock private Meeting meeting;
    @Mock private EntityType entityType;
    @Mock private SearchQuery searchQuery;

    private EventDetailsImpl impl;

    @Before
    public void setUp() {
        impl = new EventDetailsImpl(meetingDao);
        when(entityType.getEntityName()).thenReturn("Event Details");
        when(meeting.getId()).thenReturn("evt-123");
        when(meeting.getTitle()).thenReturn("Test Meeting");
        when(meeting.getDateFrom()).thenReturn(1700000000000L);
        when(meeting.getDateTo()).thenReturn(1700003600000L);
        when(meeting.getLocation()).thenReturn("Room A");
        when(meeting.getDetails()).thenReturn("Details here");
        when(meeting.getTimeZone()).thenReturn("UTC");
        when(meeting.getOrganizer()).thenReturn("org@test.com");
        when(meeting.getContacts()).thenReturn(Collections.singletonList("a@test.com"));
    }

    // ── create() ────────────────────────────────────────────────────────────────

    @Test
    public void create_validInputs_returnsMappedEntityValue() {
        when(meetingDao.addMeeting(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(meeting);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Test Meeting");
        fields.put("Date From", 1700000000000L);
        fields.put("Date To", 1700003600000L);

        EntityValue result = impl.create(entityType, fields);

        assertNotNull(result);
        assertEquals("Event Details", result.getEntityName());
        assertEquals("evt-123", result.getFields().get("Event Id"));
    }

    @Test
    public void create_withAllOptionalFields_passesThemToDao() {
        when(meetingDao.addMeeting(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(meeting);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Full Meeting");
        fields.put("Date From", 1700000000000L);
        fields.put("Date To", 1700003600000L);
        fields.put("Location", "Room B");
        fields.put("Details", "Agenda here");
        fields.put("Time Zone", "America/New_York");
        fields.put("Contacts", Arrays.asList("a@test.com", "b@test.com"));

        EntityValue result = impl.create(entityType, fields);

        assertNotNull(result);
        verify(meetingDao).addMeeting(eq("Full Meeting"), eq("Room B"), eq(1700000000000L),
                eq(1700003600000L), any(), any(), any(), eq("America/New_York"),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), eq("Agenda here"), any());
    }

    @Test
    public void create_contactsAsSingleString_wrappedInList() {
        when(meetingDao.addMeeting(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(meeting);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Test");
        fields.put("Date From", 1700000000000L);
        fields.put("Date To", 1700003600000L);
        fields.put("Contacts", "single@test.com");

        EntityValue result = impl.create(entityType, fields);
        assertNotNull(result);
    }

    @Test
    public void create_nullDaoResult_returnsEmptyFields() {
        when(meetingDao.addMeeting(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(null);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Test");
        fields.put("Date From", 1700000000000L);
        fields.put("Date To", 1700003600000L);

        EntityValue result = impl.create(entityType, fields);
        assertNotNull(result);
        assertNull(result.getFields().get("Event Id"));
    }

    @Test
    public void create_dateFromAsString_parsedToLong() {
        when(meetingDao.addMeeting(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(meeting);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Test");
        fields.put("Date From", "1700000000000");
        fields.put("Date To", "1700003600000");

        EntityValue result = impl.create(entityType, fields);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_missingTitle_throws() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("Date From", 1700000000000L);
        fields.put("Date To", 1700003600000L);
        impl.create(entityType, fields);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_missingDateFrom_throws() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Test");
        fields.put("Date To", 1700003600000L);
        impl.create(entityType, fields);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_missingDateTo_throws() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Test");
        fields.put("Date From", 1700000000000L);
        impl.create(entityType, fields);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_nullTitleValue_throws() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", null);
        fields.put("Date From", 1700000000000L);
        fields.put("Date To", 1700003600000L);
        impl.create(entityType, fields);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_nonParsableDateFrom_throwsBecauseToLongReturnsNull() {
        when(meetingDao.addMeeting(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(meeting);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Title", "Test");
        fields.put("Date From", "not-a-number");
        fields.put("Date To", 1700003600000L);
        impl.create(entityType, fields);
    }

    // ── update() ────────────────────────────────────────────────────────────────

    @Test
    public void update_validEventId_returnsUpdatedEntityValue() {
        when(meetingDao.getMeetingById("evt-123")).thenReturn(meeting);
        when(meetingDao.updateMeeting(eq("evt-123"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(meeting);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Event Id", "evt-123");
        fields.put("Title", "Updated Title");
        EntityValue entity = new EntityValue("Event Details", fields);

        EntityValue result = impl.update(entityType, entity);

        assertNotNull(result);
        assertEquals("Event Details", result.getEntityName());
        assertEquals("evt-123", result.getFields().get("Event Id"));
    }

    @Test
    public void update_nullUpdateResult_returnsEmptyFields() {
        when(meetingDao.getMeetingById("evt-123")).thenReturn(meeting);
        when(meetingDao.updateMeeting(eq("evt-123"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Event Id", "evt-123");
        EntityValue entity = new EntityValue("Event Details", fields);

        EntityValue result = impl.update(entityType, entity);
        assertNotNull(result);
        assertNull(result.getFields().get("Event Id"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_missingEventId_throws() {
        EntityValue entity = new EntityValue("Event Details", new HashMap<>());
        impl.update(entityType, entity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_nullFields_throws() {
        EntityValue entity = new EntityValue("Event Details", null);
        impl.update(entityType, entity);
    }

    @Test(expected = NoSuchElementException.class)
    public void update_eventNotFound_throws() {
        when(meetingDao.getMeetingById("missing")).thenReturn(null);
        Map<String, Object> fields = new HashMap<>();
        fields.put("Event Id", "missing");
        EntityValue entity = new EntityValue("Event Details", fields);
        impl.update(entityType, entity);
    }

    // ── get() ────────────────────────────────────────────────────────────────────

    @Test
    public void get_existingEvent_returnsEntityValue() {
        when(meetingDao.getMeetingById("evt-123")).thenReturn(meeting);

        EntityValue result = impl.get(entityType, "evt-123");

        assertNotNull(result);
        assertEquals("Event Details", result.getEntityName());
        assertEquals("evt-123", result.getFields().get("Event Id"));
        assertEquals("Test Meeting", result.getFields().get("Title"));
    }

    @Test(expected = NoSuchElementException.class)
    public void get_notFound_throws() {
        when(meetingDao.getMeetingById("missing")).thenReturn(null);
        impl.get(entityType, "missing");
    }

    // ── search() ─────────────────────────────────────────────────────────────────

    @Test
    public void search_nullQuery_fetchesMeetingsWithDefaultPageSize() {
        when(meetingDao.getMeetings(50)).thenReturn(Collections.singletonList(meeting));

        List<EntityValue> result = impl.search(entityType, null, null, null);

        assertEquals(1, result.size());
        assertEquals("evt-123", result.get(0).getFields().get("Event Id"));
        verify(meetingDao).getMeetings(50);
    }

    @Test
    public void search_emptyConditions_usesGetMeetings() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetings(50)).thenReturn(Collections.singletonList(meeting));

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(1, result.size());
        verify(meetingDao).getMeetings(50);
    }

    @Test
    public void search_customPageSize_passedToGetMeetings() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetings(20)).thenReturn(Collections.emptyList());

        impl.search(entityType, searchQuery, 1L, 20);

        verify(meetingDao).getMeetings(20);
    }

    @Test
    public void search_singleEventIdEquals_usesDirectGet() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("Event Id");
        when(cond.getOperand()).thenReturn("evt-123");
        when(cond.getOperator()).thenReturn(SearchCondition.Operator.equals);
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetingById("evt-123")).thenReturn(meeting);

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(1, result.size());
        verify(meetingDao).getMeetingById("evt-123");
        verify(meetingDao, never()).getMeetings(anyInt());
    }

    @Test
    public void search_singleEventIdNullOperator_treatedAsEquals() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("Event Id");
        when(cond.getOperand()).thenReturn("evt-123");
        when(cond.getOperator()).thenReturn(null);
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetingById("evt-123")).thenReturn(meeting);

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(1, result.size());
        verify(meetingDao).getMeetingById("evt-123");
    }

    @Test
    public void search_singleEventIdNotFound_returnsEmpty() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("Event Id");
        when(cond.getOperand()).thenReturn("missing");
        when(cond.getOperator()).thenReturn(SearchCondition.Operator.equals);
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetingById("missing")).thenReturn(null);

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    public void search_organizerWithDateRange_usesGetEventsForUser() {
        SearchCondition orgCond = mock(SearchCondition.class);
        SearchCondition fromCond = mock(SearchCondition.class);
        SearchCondition toCond = mock(SearchCondition.class);
        when(orgCond.getFieldName()).thenReturn("organizer");
        when(orgCond.getOperand()).thenReturn("user@test.com");
        when(fromCond.getFieldName()).thenReturn("date from");
        when(fromCond.getOperand()).thenReturn(1700000000000L);
        when(toCond.getFieldName()).thenReturn("date to");
        when(toCond.getOperand()).thenReturn(1700003600000L);
        when(searchQuery.getSearchConditions()).thenReturn(Arrays.asList(orgCond, fromCond, toCond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getEventsForUser("user@test.com", 1700000000000L, 1700003600000L))
                .thenReturn(Collections.singletonList(meeting));

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(1, result.size());
        verify(meetingDao).getEventsForUser("user@test.com", 1700000000000L, 1700003600000L);
        verify(meetingDao, never()).getQueryResults(any(), any(), any());
    }

    @Test
    public void search_dateRangeOnly_usesQueryResultsAndResolves() {
        SearchCondition fromCond = mock(SearchCondition.class);
        SearchCondition toCond = mock(SearchCondition.class);
        when(fromCond.getFieldName()).thenReturn("date from");
        when(fromCond.getOperand()).thenReturn(1700000000000L);
        when(toCond.getFieldName()).thenReturn("date to");
        when(toCond.getOperand()).thenReturn(1700003600000L);
        when(searchQuery.getSearchConditions()).thenReturn(Arrays.asList(fromCond, toCond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());

        Map<String, Object> hit = new HashMap<>();
        hit.put("Event ID", "evt-123");
        when(meetingDao.getQueryResults(null, 1700000000000L, 1700003600000L))
                .thenReturn(Collections.singletonList(hit));
        when(meetingDao.getMeetingById("evt-123")).thenReturn(meeting);

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(1, result.size());
        verify(meetingDao).getQueryResults(null, 1700000000000L, 1700003600000L);
    }

    @Test
    public void search_keywordTitleQuery_usesQueryResultsAndResolves() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("title");
        when(cond.getOperand()).thenReturn("standup");
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());

        Map<String, Object> hit = new HashMap<>();
        hit.put("Event ID", "evt-123");
        when(meetingDao.getQueryResults("standup", null, null))
                .thenReturn(Collections.singletonList(hit));
        when(meetingDao.getMeetingById("evt-123")).thenReturn(meeting);

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(1, result.size());
        verify(meetingDao).getQueryResults("standup", null, null);
    }

    @Test
    public void search_multipleKeywords_concatenated() {
        SearchCondition c1 = mock(SearchCondition.class);
        SearchCondition c2 = mock(SearchCondition.class);
        when(c1.getFieldName()).thenReturn("title");
        when(c1.getOperand()).thenReturn("standup");
        when(c2.getFieldName()).thenReturn("details");
        when(c2.getOperand()).thenReturn("weekly");
        when(searchQuery.getSearchConditions()).thenReturn(Arrays.asList(c1, c2));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getQueryResults("standup weekly", null, null))
                .thenReturn(Collections.emptyList());

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertTrue(result.isEmpty());
        verify(meetingDao).getQueryResults("standup weekly", null, null);
    }

    @Test
    public void search_hitsWithNullEventId_skipped() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("title");
        when(cond.getOperand()).thenReturn("test");
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());

        Map<String, Object> hitWithNull = new HashMap<>();
        hitWithNull.put("Event ID", null);
        Map<String, Object> hitWithId = new HashMap<>();
        hitWithId.put("Event ID", "evt-123");
        when(meetingDao.getQueryResults("test", null, null))
                .thenReturn(Arrays.asList(hitWithNull, hitWithId));
        when(meetingDao.getMeetingById("evt-123")).thenReturn(meeting);

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(1, result.size());
    }

    @Test
    public void search_emptyHits_returnsEmpty() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("title");
        when(cond.getOperand()).thenReturn("nothing");
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getQueryResults("nothing", null, null)).thenReturn(Collections.emptyList());

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    public void search_nullHitsFromDao_returnsEmpty() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("title");
        when(cond.getOperand()).thenReturn("test");
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getQueryResults("test", null, null)).thenReturn(null);

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    public void search_conditionWithNullOperand_skipped() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("title");
        when(cond.getOperand()).thenReturn(null);
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetings(50)).thenReturn(Collections.emptyList());

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertTrue(result.isEmpty());
        verify(meetingDao).getMeetings(50);
    }

    @Test
    public void search_secondPage_skipsFirstPageResults() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());

        List<Meeting> fiveMeetings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meeting m = mock(Meeting.class);
            when(m.getId()).thenReturn("evt-" + i);
            fiveMeetings.add(m);
        }
        when(meetingDao.getMeetings(anyInt())).thenReturn(fiveMeetings);

        // page 2, size 3 → skip first 3, return remaining 2
        List<EntityValue> result = impl.search(entityType, searchQuery, 2L, 3);

        assertEquals(2, result.size());
    }

    @Test
    public void search_orderByDateFromAsc_sortsCorrectly() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        QueryClause clause = mock(QueryClause.class);
        when(clause.getOperator()).thenReturn(QueryClause.Operator.orderby);
        when(clause.getFieldName()).thenReturn("date from");
        when(clause.getOperand()).thenReturn("ASC");
        when(searchQuery.getQueryClauses()).thenReturn(Collections.singletonList(clause));

        Meeting m1 = mock(Meeting.class);
        Meeting m2 = mock(Meeting.class);
        when(m1.getId()).thenReturn("a");
        when(m1.getDateFrom()).thenReturn(2000L);
        when(m2.getId()).thenReturn("b");
        when(m2.getDateFrom()).thenReturn(1000L);
        when(meetingDao.getMeetings(anyInt())).thenReturn(Arrays.asList(m1, m2));

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(2, result.size());
        assertEquals("b", result.get(0).getFields().get("Event Id"));
        assertEquals("a", result.get(1).getFields().get("Event Id"));
    }

    @Test
    public void search_orderByDateFromDesc_sortsCorrectly() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        QueryClause clause = mock(QueryClause.class);
        when(clause.getOperator()).thenReturn(QueryClause.Operator.orderby);
        when(clause.getFieldName()).thenReturn("date from");
        when(clause.getOperand()).thenReturn("DESC");
        when(searchQuery.getQueryClauses()).thenReturn(Collections.singletonList(clause));

        Meeting m1 = mock(Meeting.class);
        Meeting m2 = mock(Meeting.class);
        when(m1.getId()).thenReturn("a");
        when(m1.getDateFrom()).thenReturn(1000L);
        when(m2.getId()).thenReturn("b");
        when(m2.getDateFrom()).thenReturn(2000L);
        when(meetingDao.getMeetings(anyInt())).thenReturn(Arrays.asList(m1, m2));

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(2, result.size());
        assertEquals("b", result.get(0).getFields().get("Event Id"));
    }

    @Test
    public void search_orderByTitle_sortsAlphabetically() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        QueryClause clause = mock(QueryClause.class);
        when(clause.getOperator()).thenReturn(QueryClause.Operator.orderby);
        when(clause.getFieldName()).thenReturn("title");
        when(clause.getOperand()).thenReturn("ASC");
        when(searchQuery.getQueryClauses()).thenReturn(Collections.singletonList(clause));

        Meeting m1 = mock(Meeting.class);
        Meeting m2 = mock(Meeting.class);
        when(m1.getId()).thenReturn("a");
        when(m1.getTitle()).thenReturn("Zulu");
        when(m2.getId()).thenReturn("b");
        when(m2.getTitle()).thenReturn("Alpha");
        when(meetingDao.getMeetings(anyInt())).thenReturn(Arrays.asList(m1, m2));

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals("b", result.get(0).getFields().get("Event Id"));
    }

    @Test
    public void search_orderByUnknownField_noSortApplied() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        QueryClause clause = mock(QueryClause.class);
        when(clause.getOperator()).thenReturn(QueryClause.Operator.orderby);
        when(clause.getFieldName()).thenReturn("unknown_field");
        when(clause.getOperand()).thenReturn("ASC");
        when(searchQuery.getQueryClauses()).thenReturn(Collections.singletonList(clause));
        when(meetingDao.getMeetings(anyInt())).thenReturn(Collections.singletonList(meeting));

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertEquals(1, result.size());
    }

    @Test
    public void search_nullMeetingsFromDao_returnsEmpty() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetings(50)).thenReturn(null);

        List<EntityValue> result = impl.search(entityType, searchQuery, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    public void search_locationCondition_addedToSearchTerms() {
        SearchCondition cond = mock(SearchCondition.class);
        when(cond.getFieldName()).thenReturn("location");
        when(cond.getOperand()).thenReturn("Room A");
        when(searchQuery.getSearchConditions()).thenReturn(Collections.singletonList(cond));
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getQueryResults("Room A", null, null)).thenReturn(Collections.emptyList());

        impl.search(entityType, searchQuery, null, null);

        verify(meetingDao).getQueryResults("Room A", null, null);
    }

    @Test
    public void search_invalidPageSize_defaultsTo50() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetings(50)).thenReturn(Collections.emptyList());

        impl.search(entityType, searchQuery, null, 0);

        verify(meetingDao).getMeetings(50);
    }

    @Test
    public void search_negativePageIndex_treatedAsFirstPage() {
        when(searchQuery.getSearchConditions()).thenReturn(Collections.emptyList());
        when(searchQuery.getQueryClauses()).thenReturn(Collections.emptyList());
        when(meetingDao.getMeetings(50)).thenReturn(Collections.singletonList(meeting));

        List<EntityValue> result = impl.search(entityType, searchQuery, -1L, null);

        assertEquals(1, result.size());
    }
}
