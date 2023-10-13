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

Note: Detail description of fields is [here](pages/details.md).

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

| **Parameter Name**      | **Parameter Type** | **Mandatory** | **Description & Example**                                                                                                        |
|-------------------------|--------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------|
| Title                   | Text               | No            | Company Update                                                                                                                   |
| Location                | Text               | No            | Pune                                                                                                                             |
| Date From               | Date               | Yes           | 5 Jun 2022 17:00                                                                                                                 |
| Date To                 | Date               | No            | 5 Jun 2022 18:00                                                                                                                 |
| Time Zone               | Text               | No            | UTC, Asia/Kolkata                                                                                                                |
| Details                 | Rich text          | No            | Share Company Update                                                                                                             |
| Required Attendee Email | Email              | No            | requiredattendee@email.com                                                                                                       |
| Optional Attendee Email | Email              | No            | optionalattendee@email.com                                                                                                       |
| Is All Day              | Yes/No             | No            | Sets the meeting for all day. For example: False, True                                                                           |
| Reminder                | Pick one           | No            | Reminding the event. For example: Don't remind me,5 min. before                                                                  |
| Recurrence              | Pick one           | No            | Repeating the event. For example: Does Not Repeat, Daily, Weekly                                                                 |
| Recurrence Interval     | Number             | No            | Time between two events. For example: 1,4,8                                                                                      |
| Days Of Week            | String             | No            | Specific day of week, on which you want to repeat an event. For example: Every Wednesday                                         |
| Recurrence Type         | Pick one           | No            | Absolute Recurrence: Exactly that day. For example: 19th,27th. Relative Recurrence: Day of that day. For example: Monday, Friday |
| Day Of Month            | Number             | No            | Specific day of month. For example: 23, 9                                                                                        |
| Repeat Index            | Pick One           | No            | Specify which day of week in monthly or yearly recurrence. For example: Second Monday of month                                   |
| Repeat Day              | Pick One           | No            | Specific day of week, that you want to repeat. For example: Wednesday                                                            |
| Repeat Month            | Pick One           | No            | Month of year, that you want to repeat. For example: April                                                                       |
| End Recurrence          | Pick One           | No            | Specify the end of recurrence. For example: End Date                                                                             |
| End Date                | Date               | No            | If you want to end on specific date. For example: 25 Apr 2025                                                                    |
| Numbered Occurrences    | Number             | No            | If you want to end after specific occurrence. For example: 25, 56                                                                |
| Attachments             | File               | No            | Any type of file                                                                                                                 |

Notes:
* Refer to the list of supported time zones, [here](https://learn.microsoft.com/en-us/graph/api/resources/datetimetimezone?view=graph-rest-1.0).<br>
* Detail description of fields is [here](pages/details.md).

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

| **Parameter Name**    | **Parameter Type** | **Mandatory** | **Example**         |
|-----------------------|--------------------|---------------|---------------------|
| Date From             | Date               | Yes           | 5 Jun 2022 10:00    |
| Date To               | Date               | Yes           | 5 Jun 2022 17:00    |
| Is Organizer Optional | Yes/No             | No            | Yes                 |
| Required Attendee     | List<Email>        | Yes           | abc@xyz.com         |
| Optional Attendee     | List<Email>        | No            | xyz@abc.com         |
| Time Zone             | String             | No            | India Standard Time |
| Meeting Duration      | String             | No            | 1H30M               |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| List of Free Slots | List<Multi Field>  |

Note: To refer additional time zones, please scroll to the end of the page and refer to the **Time Zones For Free Slots
**
section.

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

### Time Zones For Free Slots

The Time Zone input parameter for the Get Free Slots request accepts the following time zone values:

| Time Zone ID                    | Time Zone Description                                         |
|---------------------------------|---------------------------------------------------------------|
| Dateline Standard Time          | (UTC-12:00) International Date Line West                      |
| Samoa Standard Time             | (UTC+13:00) Samoa                                             |
| UTC-11                          | (UTC-11:00) Coordinated Universal Time-11                     |
| Aleutian Standard Time          | (UTC-10:00) Aleutian Islands                                  |
| Hawaiian Standard Time          | (UTC-10:00) Hawaii                                            |
| Marquesas Standard Time         | (UTC-09:30) Marquesas Islands                                 |
| Alaskan Standard Time           | (UTC-09:00) Alaska                                            |
| UTC-09                          | (UTC-09:00) Coordinated Universal Time-09                     |
| Yukon Standard Time             | (UTC-07:00) Yukon                                             |
| Pacific Standard Time           | (UTC-08:00) Baja California (Mexico)                          |
| UTC-08                          | (UTC-08:00) Coordinated Universal Time-08                     |
| Pacific Standard Time           | (UTC-08:00) Pacific Time (US & Canada)                        |
| US Mountain Standard Time       | (UTC-07:00) Arizona                                           |
| Mountain Standard Time          | (UTC-07:00) La Paz, Mazatlan (Mexico)                         |
| Mountain Standard Time          | (UTC-07:00) Mountain Time (US & Canada)                       |
| Eastern Standard Time           | (UTC-05:00) Chetumal (Mexico)                                 |
| Central America Standard Time   | (UTC-06:00) Central America                                   |
| Central Standard Time           | (UTC-06:00) Central Time (US & Canada)                        |
| Easter Island Standard Time     | (UTC-06:00) Easter Island                                     |
| Central Standard Time           | (UTC-06:00) Guadalajara, Mexico City, Monterrey               |
| Canada Central Standard Time    | (UTC-06:00) Saskatchewan                                      |
| SA Pacific Standard Time        | (UTC-05:00) Bogota, Lima, Quito, Rio Branco                   |
| Eastern Standard Time           | (UTC-05:00) Eastern Time (US & Canada)                        |
| Haiti Standard Time             | (UTC-05:00) Haiti                                             |
| Cuba Standard Time              | (UTC-05:00) Havana                                            |
| US Eastern Standard Time        | (UTC-05:00) Indiana (East)                                    |
| Turks And Caicos Standard Time  | (UTC-05:00) Turks and Caicos                                  |
| Venezuela Standard Time         | (UTC-04:00) Caracas                                           |
| Magallanes Standard Time        | (UTC-03:00) Punta Arenas                                      |
| Paraguay Standard Time          | (UTC-04:00) Asuncion                                          |
| Atlantic Standard Time          | (UTC-04:00) Atlantic Time (Canada)                            |
| Central Brazilian Standard Time | (UTC-04:00) Cuiaba                                            |
| SA Western Standard Time        | (UTC-04:00) Georgetown, La Paz, Manaus, San Juan              |
| Pacific SA Standard Time        | (UTC-04:00) Santiago                                          |
| Newfoundland Standard Time      | (UTC-03:30) Newfoundland                                      |
| Tocantins Standard Time         | (UTC-03:00) Araguaina                                         |
| E. South America Standard Time  | (UTC-03:00) Brasilia                                          |
| SA Eastern Standard Time        | (UTC-03:00) Cayenne, Fortaleza                                |
| Argentina Standard Time         | (UTC-03:00) City of Buenos Aires                              |
| Greenland Standard Time         | (UTC-03:00) Greenland                                         |
| Montevideo Standard Time        | (UTC-03:00) Montevideo                                        |
| Saint Pierre Standard Time      | (UTC-03:00) Saint Pierre and Miquelon                         |
| Bahia Standard Time             | (UTC-03:00) Salvador                                          |
| UTC-02                          | (UTC-02:00) Coordinated Universal Time-02                     |
| Mid-Atlantic Standard Time      | (UTC-02:00) Mid-Atlantic - Old                                |
| Azores Standard Time            | (UTC-01:00) Azores                                            |
| Cape Verde Standard Time        | (UTC-01:00) Cabo Verde Is.                                    |
| UTC                             | (UTC) Coordinated Universal Time                              |
| GMT Standard Time               | (UTC+00:00) Dublin, Edinburgh, Lisbon, London                 |
| Greenwich Standard Time         | (UTC+00:00) Monrovia, Reykjavik                               |
| Morocco Standard Time           | (UTC+01:00) Casablanca                                        |
| W. Europe Standard Time         | (UTC+01:00) Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna  |
| Central Europe Standard Time    | (UTC+01:00) Belgrade, Bratislava, Budapest, Ljubljana, Prague |
| Romance Standard Time           | (UTC+01:00) Brussels, Copenhagen, Madrid, Paris               |
| Central European Standard Time  | (UTC+01:00) Sarajevo, Skopje, Warsaw, Zagreb                  |
| W. Central Africa Standard Time | (UTC+01:00) West Central Africa                               |
| Libya Standard Time             | (UTC+02:00) Tripoli                                           |
| Namibia Standard Time           | (UTC+02:00) Windhoek                                          |
| GTB Standard Time               | (UTC+02:00) Athens, Bucharest                                 |
| Middle East Standard Time       | (UTC+02:00) Beirut                                            |
| Egypt Standard Time             | (UTC+02:00) Cairo                                             |
| E. Europe Standard Time         | (UTC+02:00) Chisinau                                          |
| Syria Standard Time             | (UTC+02:00) Damascus                                          |
| West Bank Standard Time         | (UTC+02:00) Gaza, Hebron                                      |
| South Africa Standard Time      | (UTC+02:00) Harare, Pretoria                                  |
| FLE Standard Time               | (UTC+02:00) Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius     |
| Israel Standard Time            | (UTC+02:00) Jerusalem                                         |
| South Sudan Standard Time       | (UTC+02:00) Juba                                              |
| Kaliningrad Standard Time       | (UTC+02:00) Kaliningrad                                       |
| Sudan Standard Time             | (UTC+02:00) Khartoum                                          |
| Jordan Standard Time            | (UTC+03:00) Amman                                             |
| Turkey Standard Time            | (UTC+03:00) Istanbul                                          |
| Belarus Standard Time           | (UTC+03:00) Minsk                                             |
| Arabic Standard Time            | (UTC+03:00) Baghdad                                           |
| Arab Standard Time              | (UTC+03:00) Kuwait, Riyadh                                    |
| Russian Standard Time           | (UTC+03:00) Moscow, St. Petersburg                            |
| E. Africa Standard Time         | (UTC+03:00) Nairobi                                           |
| Volgograd Standard Time         | (UTC+03:00) Volgograd                                         |
| Astrakhan Standard Time         | (UTC+04:00) Astrakhan, Ulyanovsk                              |
| Russia Time Zone 3              | (UTC+04:00) Izhevsk, Samara                                   |
| Saratov Standard Time           | (UTC+04:00) Saratov                                           |
| Iran Standard Time              | (UTC+03:30) Tehran                                            |
| Arabian Standard Time           | (UTC+04:00) Abu Dhabi, Muscat                                 |
| Azerbaijan Standard Time        | (UTC+04:00) Baku                                              |
| Mauritius Standard Time         | (UTC+04:00) Port Louis                                        |
| Georgian Standard Time          | (UTC+04:00) Tbilisi                                           |
| Caucasus Standard Time          | (UTC+04:00) Yerevan                                           |
| Afghanistan Standard Time       | (UTC+04:30) Kabul                                             |
| West Asia Standard Time         | (UTC+05:00) Ashgabat, Tashkent                                |
| Ekaterinburg Standard Time      | (UTC+05:00) Ekaterinburg                                      |
| Pakistan Standard Time          | (UTC+05:00) Islamabad, Karachi                                |
| Qyzylorda Standard Time         | (UTC+05:00) Qyzylorda                                         |
| India Standard Time             | (UTC+05:30) Chennai, Kolkata, Mumbai, New Delhi               |
| Sri Lanka Standard Time         | (UTC+05:30) Sri Jayawardenepura                               |
| Nepal Standard Time             | (UTC+05:45) Kathmandu                                         |
| Central Asia Standard Time      | (UTC+06:00) Astana                                            |
| Bangladesh Standard Time        | (UTC+06:00) Dhaka                                             |
| Omsk Standard Time              | (UTC+06:00) Omsk                                              |
| Altai Standard Time             | (UTC+07:00) Barnaul, Gorno-Altaysk                            |
| N. Central Asia Standard Time   | (UTC+07:00) Novosibirsk                                       |
| Tomsk Standard Time             | (UTC+07:00) Tomsk                                             |
| Myanmar Standard Time           | (UTC+06:30) Yangon (Rangoon)                                  |
| SE Asia Standard Time           | (UTC+07:00) Bangkok, Hanoi, Jakarta                           |
| W. Mongolia Standard Time       | (UTC+07:00) Hovd                                              |
| North Asia Standard Time        | (UTC+07:00) Krasnoyarsk                                       |
| China Standard Time             | (UTC+08:00) Beijing, Chongqing, Hong Kong, Urumqi             |
| North Asia East Standard Time   | (UTC+08:00) Irkutsk                                           |
| Singapore Standard Time         | (UTC+08:00) Kuala Lumpur, Singapore                           |
| W. Australia Standard Time      | (UTC+08:00) Perth                                             |
| Taipei Standard Time            | (UTC+08:00) Taipei                                            |
| Ulaanbaatar Standard Time       | (UTC+08:00) Ulaanbaatar                                       |
| Transbaikal Standard Time       | (UTC+09:00) Chita                                             |
| North Korea Standard Time       | (UTC+09:00) Pyongyang                                         |
| Aus Central W. Standard Time    | (UTC+08:45) Eucla                                             |
| Tokyo Standard Time             | (UTC+09:00) Osaka, Sapporo, Tokyo                             |
| Korea Standard Time             | (UTC+09:00) Seoul                                             |
| Yakutsk Standard Time           | (UTC+09:00) Yakutsk                                           |
| Cen. Australia Standard Time    | (UTC+09:30) Adelaide                                          |
| AUS Central Standard Time       | (UTC+09:30) Darwin                                            |
| E. Australia Standard Time      | (UTC+10:00) Brisbane                                          |
| AUS Eastern Standard Time       | (UTC+10:00) Canberra, Melbourne, Sydney                       |
| West Pacific Standard Time      | (UTC+10:00) Guam, Port Moresby                                |
| Tasmania Standard Time          | (UTC+10:00) Hobart                                            |
| Vladivostok Standard Time       | (UTC+10:00) Vladivostok                                       |
| Bougainville Standard Time      | (UTC+11:00) Bougainville Island                               |
| Magadan Standard Time           | (UTC+11:00) Magadan                                           |
| Sakhalin Standard Time          | (UTC+11:00) Sakhalin                                          |
| Lord Howe Standard Time         | (UTC+10:30) Lord Howe Island                                  |
| Russia Time Zone 10             | (UTC+11:00) Chokurdakh                                        |
| Norfolk Standard Time           | (UTC+11:00) Norfolk Island                                    |
| Central Pacific Standard Time   | (UTC+11:00) Solomon Is., New Caledonia                        |
| Russia Time Zone 11             | (UTC+12:00) Anadyr, Petropavlovsk-Kamchatsky                  |
| New Zealand Standard Time       | (UTC+12:00) Auckland, Wellington                              |
| UTC+12                          | (UTC+12:00) Coordinated Universal Time+12                     |
| Fiji Standard Time              | (UTC+12:00) Fiji                                              |
| Kamchatka Standard Time         | (UTC+12:00) Petropavlovsk-Kamchatsky - Old                    |
| Chatham Islands Standard Time   | (UTC+12:45) Chatham Islands                                   |
| UTC+13                          | (UTC+13:00) Coordinated Universal Time+13                     |
| Tonga Standard Time             | (UTC+13:00) Nuku'alofa                                        |
| Line Islands Standard Time      | (UTC+14:00) Kiritimati Island                                 |






