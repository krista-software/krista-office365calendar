package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MeetingDaoImplTest {

    @Mock
    private GraphServiceClientProvider clientProvider;

    @Mock
    private Office365CalendarAttributes attrs;

    private MeetingDaoImpl dao;

    @Before
    public void setUp() {
        Mockito.when(attrs.getTimeZone()).thenReturn("UTC");
        dao = new MeetingDaoImpl(clientProvider, attrs);
    }

    @Test
    public void testGetTimeRangeQueryOptions_buildsCorrectOptions() {
        long start = 1700000000000L; // fixed epoch
        long end = start + 3600_000L;
        List<Option> opts = MeetingDaoImpl.getTimeRangeQueryOptions(start, end);
        Assert.assertEquals(4, opts.size());
        // Names are $top/$orderby and startDateTime/endDateTime
        Set<String> names = new HashSet<>();
        opts.forEach(o -> names.add(o.getName()));
        Assert.assertTrue(names.contains("startDateTime"));
        Assert.assertTrue(names.contains("endDateTime"));
        Assert.assertTrue(names.contains("$top"));
        Assert.assertTrue(names.contains("$orderby"));
        // Values should be ISO_OFFSET_DATE_TIME
        String startVal = opts.stream().filter(o -> o.getName().equals("startDateTime")).findFirst().get().getValue().toString();
        String endVal = opts.stream().filter(o -> o.getName().equals("endDateTime")).findFirst().get().getValue().toString();
        Assert.assertTrue(startVal.contains("T"));
        Assert.assertTrue(endVal.contains("T"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMeetings_invalidNumberOfMeetings_throws() {
        dao.getMeetings(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcceptMeetingInvitation_invalidId_throws() {
        dao.acceptMeetingInvitation(" ");
    }

    @Test
    public void testGetUnAcceptedMeetings_returnsExpectedMeeting() throws Exception {
        // Arrange: mock chain for calendarView().buildRequest(queryOptions).get()
        UserRequestBuilder userRequestBuilder = Mockito.mock(UserRequestBuilder.class);
        CalendarRequestBuilder calendarRequestBuilder = Mockito.mock(CalendarRequestBuilder.class);
        EventCollectionRequestBuilder eventCollectionRequestBuilder = Mockito.mock(EventCollectionRequestBuilder.class);
        EventCollectionRequest eventCollectionRequest = Mockito.mock(EventCollectionRequest.class);
        EventCollectionPage eventCollectionPage = Mockito.mock(EventCollectionPage.class);
        UserRequest userRequest = Mockito.mock(UserRequest.class);
        User user = new User();
        user.mail = "test.user@example.com";

        Mockito.when(clientProvider.getUserRequestBuilder(false, null)).thenReturn(userRequestBuilder);
        Mockito.when(userRequestBuilder.calendar()).thenReturn(calendarRequestBuilder);
        Mockito.when(calendarRequestBuilder.calendarView()).thenReturn(eventCollectionRequestBuilder);
        Mockito.when(eventCollectionRequestBuilder.buildRequest(ArgumentMatchers.anyList())).thenReturn(eventCollectionRequest);
        Mockito.when(eventCollectionRequest.get()).thenReturn(eventCollectionPage);
        Mockito.when(eventCollectionPage.getNextPage()).thenReturn(null);

        // For current user fetch
        Mockito.when(userRequestBuilder.buildRequest()).thenReturn(userRequest);
        Mockito.when(userRequest.get()).thenReturn(user);

        // Prepare a future event where current user is attendee and not accepted/declined
        Event e = new Event();
        e.subject = "Planning";
        e.start = new DateTimeTimeZone();
        e.end = new DateTimeTimeZone();
        e.start.timeZone = "UTC";
        e.end.timeZone = "UTC";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.DAY_OF_YEAR, 10);
        e.start.dateTime = fmt.format(cal.getTime());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        e.end.dateTime = fmt.format(cal.getTime());
        // organizer different from user
        e.organizer = new Recipient();
        e.organizer.emailAddress = new EmailAddress();
        e.organizer.emailAddress.address = "organizer@example.com";
        // attendee = current user with no response
        Attendee attendee = new Attendee();
        attendee.emailAddress = new EmailAddress();
        attendee.emailAddress.address = user.mail;
        attendee.status = new ResponseStatus();
        attendee.status.response = ResponseType.NONE;
        e.attendees = new LinkedList<>();
        e.attendees.add(attendee);

        Mockito.when(eventCollectionPage.getCurrentPage()).thenReturn(Collections.singletonList(e));

        // Act
        List<app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting> list = dao.getUnAcceptedMeetings();

        // Assert
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("Planning", list.get(0).getTitle());
    }

    @Test
    public void testPrivate_getSearchRequests_buildsEventQuery() throws Exception {
        Method m = MeetingDaoImpl.class.getDeclaredMethod("getSearchRequests", String.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        LinkedList<SearchRequest> reqs = (LinkedList<SearchRequest>) m.invoke(null, "team sync");
        Assert.assertEquals(1, reqs.size());
        SearchRequest r = reqs.getFirst();
        Assert.assertNotNull(r.query);
        Assert.assertEquals("team sync", r.query.queryString);
        Assert.assertNotNull(r.entityTypes);
        Assert.assertTrue(r.entityTypes.contains(EntityType.EVENT));
    }

    @Test(expected = IllegalStateException.class)
    public void testPrivate_getResultFromSearchResponse_emptyThrows() throws Exception {
        Method m = MeetingDaoImpl.class.getDeclaredMethod("getResultFromSearchResponse", SearchResponse.class);
        m.setAccessible(true);
        SearchResponse resp = new SearchResponse();
        // hitsContainers left null/empty -> should throw
        try {
            m.invoke(dao, resp);
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) ite.getCause();
            }
            throw ite;
        }
    }
}

