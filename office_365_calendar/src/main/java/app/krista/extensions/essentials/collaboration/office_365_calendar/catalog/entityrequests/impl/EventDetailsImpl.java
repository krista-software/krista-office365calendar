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
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.CatalogTypes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entity.EventDetails;
import app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.MeetingDaoImpl;
import app.krista.extensions.essentials.collaboration.office_365_calendar.service.Meeting;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Entity-store handler for the {@code Event Details} catalog entity.
 * Bridges generic entity-store CRUD calls to the Microsoft Graph-backed
 * {@link MeetingDaoImpl}.
 */
public final class EventDetailsImpl extends EntityImplementationService {

    private static final String EVENT_ID = "Event Id";
    private static final String TITLE = "Title";
    private static final String LOCATION = "Location";
    private static final String CONTACTS = "Contacts";
    private static final String DETAILS = "Details";
    private static final String DATE_FROM = "Date From";
    private static final String DATE_TO = "Date To";
    private static final String TIME_ZONE = "Time Zone";
    private static final String ORGANIZER = "Organizer";

    public EventDetailsImpl(MeetingDaoImpl meetingDao) {
        super(meetingDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        validateInputs(entityAttributeField, List.of(TITLE, DATE_FROM, DATE_TO));

        String title = String.valueOf(entityAttributeField.get(TITLE));
        Long dateFrom = toLong(entityAttributeField.get(DATE_FROM));
        Long dateTo = toLong(entityAttributeField.get(DATE_TO));
        String location = stringOrEmpty(entityAttributeField.get(LOCATION));
        String details = stringOrEmpty(entityAttributeField.get(DETAILS));
        String timeZone = stringOrNull(entityAttributeField.get(TIME_ZONE));
        List<String> contacts = asStringList(entityAttributeField.get(CONTACTS));

        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("Date From and Date To are required");
        }

        Meeting meeting = getMeetingDao().addMeeting(
                title,
                location,
                dateFrom,
                dateTo,
                contacts,
                Collections.emptyList(),
                Boolean.FALSE,
                timeZone,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                details,
                Boolean.FALSE);

        EventDetails created = CatalogTypes.fromEvent(meeting);
        return new EntityValue(entityType.getEntityName(), created != null ? created.toFields() : new EventDetails().toFields());
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        Map<String, Object> fields = entity.getFields();
        if (fields == null || fields.get(EVENT_ID) == null) {
            throw new IllegalArgumentException("Event Id is required for update");
        }

        String eventId = String.valueOf(fields.get(EVENT_ID));
        Meeting existing = getMeetingDao().getMeetingById(eventId);
        if (existing == null) {
            throw new NoSuchElementException("Event not found for id: " + eventId);
        }

        String title = stringOrNull(fields.get(TITLE));
        String details = stringOrNull(fields.get(DETAILS));
        String location = stringOrNull(fields.get(LOCATION));
        Long dateFrom = toLong(fields.get(DATE_FROM));
        Long dateTo = toLong(fields.get(DATE_TO));
        String timeZone = stringOrNull(fields.get(TIME_ZONE));
        List<String> contacts = asStringList(fields.get(CONTACTS));

        Meeting updated = getMeetingDao().updateMeeting(
                eventId,
                title,
                details,
                location,
                dateFrom,
                dateTo,
                timeZone,
                contacts,
                Collections.emptyList(),
                Boolean.FALSE,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Boolean.FALSE);

        EventDetails details0 = CatalogTypes.fromEvent(updated);
        return new EntityValue(entityType.getEntityName(), details0 != null ? details0.toFields() : new EventDetails().toFields());
    }

    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        Meeting meeting = getMeetingDao().getMeetingById(primaryKey);
        if (meeting == null) {
            throw new NoSuchElementException("Event not found for id: " + primaryKey);
        }
        EventDetails eventDetails = CatalogTypes.fromEvent(meeting);
        return new EntityValue(entityType.getEntityName(), eventDetails.toFields());
    }

    @Override
    public List<EntityValue> search(EntityType entityType, SearchQuery searchQuery, Long pageIndex, Integer pageSize) {
        List<SearchCondition> conditions = searchQuery != null ? searchQuery.getSearchConditions() : Collections.emptyList();
        if (conditions == null) conditions = Collections.emptyList();
        List<QueryClause> clauses = searchQuery != null ? searchQuery.getQueryClauses() : Collections.emptyList();
        if (clauses == null) clauses = Collections.emptyList();

        int effectiveSize = (pageSize == null || pageSize <= 0) ? 50 : pageSize;
        long effectivePage = (pageIndex == null || pageIndex < 1) ? 1L : pageIndex;
        long skipSize = Math.max((effectivePage - 1) * effectiveSize, 0L);

        // Fast-path: single equals on Event Id → direct GET
        if (conditions.size() == 1) {
            SearchCondition only = conditions.get(0);
            if (EVENT_ID.equalsIgnoreCase(only.getFieldName())
                    && only.getOperand() != null
                    && (only.getOperator() == null || only.getOperator() == SearchCondition.Operator.equals)) {
                String id = String.valueOf(only.getOperand()).trim();
                Meeting meeting = getMeetingDao().getMeetingById(id);
                if (meeting == null) return List.of();
                EventDetails ed = CatalogTypes.fromEvent(meeting);
                return List.of(new EntityValue(entityType.getEntityName(), ed.toFields()));
            }
        }

        // Parse conditions into supported filter buckets
        Long dateFrom = null;
        Long dateTo = null;
        String organizerEmail = null;
        StringBuilder searchTerms = new StringBuilder();
        for (SearchCondition sc : conditions) {
            String fieldName = sc.getFieldName();
            Object operand = sc.getOperand();
            if (fieldName == null || operand == null) continue;
            String value = String.valueOf(operand).trim();
            if (value.isEmpty()) continue;
            switch (fieldName.toLowerCase(Locale.ROOT)) {
                case "date from":
                    dateFrom = toLong(operand);
                    break;
                case "date to":
                    dateTo = toLong(operand);
                    break;
                case "organizer":
                    organizerEmail = value;
                    break;
                case "title":
                case "details":
                case "location":
                    if (searchTerms.length() > 0) searchTerms.append(' ');
                    searchTerms.append(value);
                    break;
                default:
                    break;
            }
        }
        String query = searchTerms.length() > 0 ? searchTerms.toString() : null;

        // Route by the shape of the filter set
        List<Meeting> meetings;
        if (organizerEmail != null && dateFrom != null && dateTo != null) {
            meetings = getMeetingDao().getEventsForUser(organizerEmail, dateFrom, dateTo);
        } else if (dateFrom != null || dateTo != null) {
            meetings = resolveMeetingsFromHits(getMeetingDao().getQueryResults(query, dateFrom, dateTo));
        } else if (query != null) {
            meetings = resolveMeetingsFromHits(getMeetingDao().getQueryResults(query, null, null));
        } else {
            int requestCount = Math.max((int) Math.min(skipSize + effectiveSize, Integer.MAX_VALUE), effectiveSize);
            meetings = getMeetingDao().getMeetings(requestCount);
        }
        if (meetings == null) meetings = Collections.emptyList();

        applyOrderBy(meetings, clauses);

        return meetings.stream()
                .filter(Objects::nonNull)
                .skip(skipSize)
                .limit(effectiveSize)
                .map(CatalogTypes::fromEvent)
                .filter(Objects::nonNull)
                .map(ed -> new EntityValue(entityType.getEntityName(), ed.toFields()))
                .collect(Collectors.toList());
    }

    /**
     * The keyword-search Graph path returns lightweight hit maps ({@code Event ID},
     * {@code Event Title}, {@code Scheduled Time}). Resolve each hit back to a full
     * {@link Meeting} so we can project a complete {@link EventDetails}.
     */
    private List<Meeting> resolveMeetingsFromHits(List<Map<String, Object>> hits) {
        if (hits == null || hits.isEmpty()) return Collections.emptyList();
        List<Meeting> result = new ArrayList<>(hits.size());
        for (Map<String, Object> hit : hits) {
            Object idObj = hit.get("Event ID");
            if (idObj == null) continue;
            Meeting m = getMeetingDao().getMeetingById(String.valueOf(idObj));
            if (m != null) result.add(m);
        }
        return result;
    }

    private void applyOrderBy(List<Meeting> meetings, List<QueryClause> clauses) {
        if (meetings == null || meetings.isEmpty() || clauses == null || clauses.isEmpty()) return;
        for (QueryClause qc : clauses) {
            if (qc.getOperator() != QueryClause.Operator.orderby) continue;
            String field = qc.getFieldName();
            if (field == null) continue;
            String dir = qc.getOperand() != null ? String.valueOf(qc.getOperand()).trim().toUpperCase(Locale.ROOT) : "DESC";
            boolean asc = "ASC".equals(dir);
            Comparator<Meeting> cmp = comparatorFor(field);
            if (cmp == null) continue;
            meetings.sort(asc ? cmp : cmp.reversed());
        }
    }

    private Comparator<Meeting> comparatorFor(String fieldName) {
        switch (fieldName.toLowerCase(Locale.ROOT)) {
            case "date from":
                return Comparator.<Meeting, Long>comparing(Meeting::getDateFrom, Comparator.nullsLast(Comparator.naturalOrder()));
            case "date to":
                return Comparator.<Meeting, Long>comparing(Meeting::getDateTo, Comparator.nullsLast(Comparator.naturalOrder()));
            case "title":
                return Comparator.<Meeting, String>comparing(m -> nullSafe(m.getTitle()));
            case "organizer":
                return Comparator.<Meeting, String>comparing(m -> nullSafe(m.getOrganizer()));
            case "location":
                return Comparator.<Meeting, String>comparing(m -> nullSafe(m.getLocation()));
            default:
                return null;
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String stringOrNull(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String stringOrEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof List<?>) {
            List<?> raw = (List<?>) value;
            List<String> out = new ArrayList<>(raw.size());
            for (Object o : raw) {
                if (o != null) out.add(String.valueOf(o));
            }
            return out;
        }
        // Allow a single string fallback to be passed as the lone contact
        return List.of(String.valueOf(value));
    }
}
