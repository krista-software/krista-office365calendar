# Supported Requests


### Catalog Requests

The Office 365 Calendar Extension supports the following catalog requests.


#### Get Event

- **Description**: This request is used to obtain the event details for a specific Event ID.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example**                                                                                                                                              |
|--------------------|--------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID           | Text               | Yes            | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |

#### Decline Invitation
- **Description**: This request is used to Decline invitation of a scheduled event.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example**                                                                                                                                              |
|--------------------|--------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID           | Text               | Yes            | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |


#### Update Event
- **Description**: This request is used to update a scheduled event.
- **Input Parameters**:

| **Parameter Name**      | **Parameter Type** | **Mandatory** | **Example**                                                                                                                                              |
|-------------------------|--------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID                | Text               | Yes           | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |
| Title                   | Text               | No            | Company Update                                                                                                                                           |
| Location                | Text               | No            | Pune                                                                                                                                                     |
| Date From               | Date               | Yes           | 5 Jun 2022 17:00                                                                                                                                         |
| Date To                 | Date               | No            | 5 Jun 2022 18:00                                                                                                                                         |
| Time Zone               | Text               | No            | Asia/Kolkata                                                                                                                                             |
| Details                 | Rich text          | No            | Share Company Update                                                                                                                                     |
| Required Attendee Email | Email              | No            | requiredattendee@email.com                                                                                                                               |
| Optional Attendee Email | Email              | No            | optionalattendee@email.com                                                                                                                               |
| Is All Day              | Yes/No             | No            | False                                                                                                                                                    |
| Reminder                | Pick one           | No            | Don't remind me                                                                                                                                          |
| Recurrence              | Pick one           | No            | Does Not Repeat                                                                                                                                          |
| Recurrence Interval     | Number             | No            | 1                                                                                                                                                        |
| Days Of Week            | String             | No            | Monday                                                                                                                                                   |
| Recurrence Type         | Pick one           | No            | Absolute Recurrence                                                                                                                                      |
| Day Of Month            | Number             | No            | 23                                                                                                                                                       |
| Repeat Index            | Pick One           | No            | Second                                                                                                                                                   |
| Repeat Day              | Pick One           | No            | Wednesday                                                                                                                                                |
| Repeat Month            | Pick One           | No            | April                                                                                                                                                    |
| End Recurrence          | Pick One           | No            | End Date                                                                                                                                                 |
| End Date                | Date               | No            | 25 Apr 2025                                                                                                                                              |
| Numbered Occurrences    | Number             | No            | 25                                                                                                                                                       |


- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |


#### Get Next Upcoming Events
- **Description**: This request gives the Next Upcoming Events.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example** |
|--------------------|--------------------|----------------|-------------|
| Number of events   | Number             | Yes            | 2           |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type**     |
|--------------------|------------------------|
| Events             | List&lt;Event Details> |


#### Add Attendees
- **Description**: This request is used to add one more attendee for the scheduled event.
- **Input Parameters**:

| **Parameter Name**      | **Parameter Type** | **Mandatory** | **Example**                                                                                                                                              |
|-------------------------|--------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID                | Text               | Yes           | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |
| Required Attendee Email | Email              | No            | requiredattendee@email.com                                                                                                                               |
| Optional Attendee Email | Email              | No            | optionalattendee@email.com                                                                                                                               |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |


#### Schedule Event
- **Description**: This request is used to schedule an event.
- **Input Parameters**:

| **Parameter Name**      | **Parameter Type** | **Mandatory** | **Example**                |
|-------------------------|--------------------|---------------|----------------------------|
| Title                   | Text               | No            | Company Update             |
| Location                | Text               | No            | Pune                       |
| Date From               | Date               | Yes           | 5 Jun 2022 17:00           |
| Date To                 | Date               | No            | 5 Jun 2022 18:00           |
| Time Zone               | Text               | No            | America/Chicago            |
| Details                 | Rich text          | No            | Share Company Update       |
| Required Attendee Email | Email              | No            | requiredattendee@email.com |
| Optional Attendee Email | Email              | No            | optionalattendee@email.com |
| Is All Day              | Yes/No             | No            | False                      |
| Reminder                | Pick one           | No            | Don't remind me            |
| Recurrence              | Pick one           | No            | Does Not Repeat            |
| Recurrence Interval     | Number             | No            | 1                          |
| Days Of Week            | String             | No            | Monday                     |
| Recurrence Type         | Pick one           | No            | Absolute Recurrence        |
| Day Of Month            | Number             | No            | 23                         |
| Repeat Index            | Pick One           | No            | Second                     |
| Repeat Day              | Pick One           | No            | Wednesday                  |
| Repeat Month            | Pick One           | No            | April                      |
| End Recurrence          | Pick One           | No            | End Date                   |
| End Date                | Date               | No            | 25 Apr 2025                |
| Numbered Occurrences    | Number             | No            | 25                         |
| Attachments             | File               | No            | sample.png                 |

- Note: Refer to the list of supported time zones, [here](https://learn.microsoft.com/en-us/graph/api/resources/datetimetimezone?view=graph-rest-1.0).


- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |


#### Tentative Invitation
- **Description**: This request is used to Accept tentative invitation of a scheduled event.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example**                                                                                                                                              |
|--------------------|--------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID           | Text               | Yes            | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |


#### Get All Unaccepted Events
- **Description**: This request will Retrieve all unaccepted events.
- **Input Parameters**: NA
- **Output Parameters**:

| **Parameter Name** | **Parameter Type**     |
|--------------------|------------------------|
| Events             | List&lt;Event Details> |


#### Accept Invitation
- **Description**: This request is used to Accept invitation for the scheduled event.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example**                                                                                                                                              |
|--------------------|--------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID           | Text               | Yes            | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |


#### Remove Attendee
- **Description**: This request remove attendee from the scheduled event.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example**                                                                                                                                              |
|--------------------|--------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID           | Text               | Yes            | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |
| Attendee Email     | Email              | Yes            | attendee@email.com                                                                                                                                       |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |


#### Cancel Event
- **Description**: This request cancels the scheduled event.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example**                                                                                                                                              |
|--------------------|--------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID           | Text               | Yes            | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Events             | Event Details      |


#### Forward Event
- **Description**: This request forward the scheduled event to given email.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example**                                                                                                                                              |
|--------------------|--------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Event ID           | Text               | Yes            | AQMkADFiMjYyYTA5LTQ2MmItNDFjNC1iY2JkLTczNjlhNzI0YTMwNQBGAAADEdYrL3nHwkqRNdVE3llzIAcA7tDz_RtZdUqq0VZKQa6MWgAAAgENAAAA7tDz_RtZdUqq0VZKQa6MWgABdQwdKwAAAA== |
| Attendee Email     | Email              | Yes            | attendee@email.com                                                                                                                                       |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| IsForwarded        | Yes/No             |

#### Get Schedules
- **Description**: This request will get list of schedules meeting slots for given email and given dates.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**              |
|--------------------|--------------------|---------------|--------------------------|
| Date From          | Date               | Yes           | 5 Jun 2022 10:00         |
| Date To            | Date               | Yes           | 5 Jun 2022 17:00         |
| Attendees          | List<Email>        | Yes           | abc@xyz.com, xyz@abc.com |
| Time Zone          | String             | No            | Pacific Standard Time    |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| List of Meetings   | List<Multi Field>  |

#### Get Free Slots
- **Description**: This request will get the list of free slots for the given attendees in the given date range.
- **Input Parameters**:

| **Parameter Name**    | **Parameter Type** | **Mandatory** | **Example**      |
|-----------------------|--------------------|---------------|------------------|
| Date From             | Date               | Yes           | 5 Jun 2022 10:00 |
| Date To               | Date               | Yes           | 5 Jun 2022 17:00 |
| Is Organizer Optional | Yes/No             | No            | Yes              |
| Required Attendee     | List<Email>        | Yes           | abc@xyz.com      |
| Optional Attendee     | List<Email>        | No            | xyz@abc.com      |
| Time Zone             | String             | No            | America/Chicago  |
| Meeting Duration      | String             | No            | 1H30M            |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| List of Free Slots | List<Multi Field>  |


#### Search Events
- **Description**: This request will retrieve the list of events that matches with the given search terms.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**  | **Example**                         |
|--------------------|--------------------|----------------|-------------------------------------|
| Search Terms       | Text               | Yes            | New Event Subject, mail@example.com |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type**   |
|--------------------|----------------------|
| Events             | List&lt;Multi Field> |

  - Note: The Search Events request returns only 25 results that match the search query.

