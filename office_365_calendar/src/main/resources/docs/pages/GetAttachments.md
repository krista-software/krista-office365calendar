# Get Attachments

## Overview

Retrieves all file attachments associated with a specific calendar event. Returns a list of files attached to the meeting.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes

## Input Parameters

| Parameter Name | Type | Required | Description |
|----------------|------|----------|-------------|
| Event ID | Text | Yes | Unique identifier of the event to retrieve attachments from |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Attachments | File List | List of file attachments from the event |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event ID is null or empty | "Event ID is required" | Provide a valid event ID |
| Event ID not found | "The provided event id does not exist" | Verify the event exists |

## Usage Examples

### Example: Get Event Attachments

**Input**:
```
Event ID: "AAMkAGI1...AAA="
```

**Output**:
```json
{
  "Attachments": [
    {
      "Name": "Agenda.pdf",
      "Size": 245678,
      "ContentType": "application/pdf"
    },
    {
      "Name": "Presentation.pptx",
      "Size": 1234567,
      "ContentType": "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    }
  ]
}
```

## Business Rules

1. **Event ID Required**: Event ID is mandatory
2. **Read-Only**: Does not modify the event
3. **All Attachments**: Returns all attachments from the event

## Limitations

1. **Event Attachments Only**: Only retrieves attachments added to the event
2. **File Size**: Subject to Office 365 attachment size limits

## Related Catalog Requests

- [Get Attachments for a given User(pages/GetAttachmentsForAGivenUser.md)
- [Get Event](pages/GetEvent.md)
- [Schedule Meeting](pages/ScheduleMeeting.md)

## See Also

- [Microsoft Graph List Attachments](https://learn.microsoft.com/en-us/graph/api/event-list-attachments)

