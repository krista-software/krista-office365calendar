
### Detail Description of Fields:

* Is All Day: This property set the meeting for all day. The values can be true or false.
* Reminder: This property is about reminding the event. The values can be 'Don't remind me',  'At time of event' or '5 minutes before'.
* Recurrence: This property is about repeating the event. The values can be 'Does not repeat', 'Daily' or 'Weekly'. If you select daily, weekly, monthly or yearly then it is mandatory to fill in all the details below according to the need.
* Recurrence Interval: This property is about the time between two events. For example: If you need the event to occur every Monday then set **Recurrence interval** to 1 and **Days of week** to Monday.
* Days of Week: This property specifies on which days of the week you want the event to occur. You can give multiple days. For example: Monday or Monday and Tuesday.
* Recurrence Type: Absolute recurrence is for exact date and relative recurrence is for that day. For example: On 19th June 2023, assume it is the third Monday of the month. If you select absolute recurrence then it will occur exactly on the 19th day of the month and if you select relative then it will occur on the third Monday of that month.
* Day of Month: This property specifies on which specific days of the month you want the event to occur. For example: 2, 3 or 4.
* Repeat index: This property is only for **Recurrence** monthly and yearly. This property specifies on which **Repeat Day** of the month the event should occur. For example: If we need an event which should occur every third Monday then I will set the Repeat Day to Monday and the repeat index to third.
* Repeat day: This property specifies on which specific day you want to repeat the event. For example: Monday or Friday.
* Repeat Month: This property specifies on which specific month you want to repeat the event. **Day of month** is needed. For example: June or November.
* End Recurrence: This property specifies that on which specific thing you want to end the recurrence. If you select end date then you have to enter the end date in **End Date** below or if you selected **Numbered Occurrences** then you have to give the number. This number will specify the total times of occurrence that will happen.
* End Date: Check **End Recurrence**
* Numbered Occurrence: Check **End Recurrence**

### Schedule Daily Recurrence Meeting:

Set the following mandatory parameters and other parameters are filled according to need:

* Start Date: Start date of recurrence.<br>
* Recurrence: Daily<br>
* Recurrence Interval: Interval between days.<br>
  For example: 1 for daily, 2 for every other day.
* Days of Week: To repeat on which days in the week. Add all days for daily recurrence.<br>
  For example: Monday, Thursday only.
* End Recurrence: How to end the recurrence.<br>
  * End Date: Give the end date to complete the recurrence of the given data.<br>
  * Numbered Occurrences: End recurrence meeting after these many recurrences.

### Schedule Weekly Recurrence Meeting:

Set the following mandatory parameters and other parameters are filled according to need:


* Start Date: Start date of recurrence.<br>
* Recurrence: Weekly<br>
* Recurrence Interval: Interval between weeks.<br>
  For example: 1 for all week, 2 for every other week.
* Days of Week: To repeat on which days in the week.<br>
  For example: Monday, Thursday only.
* End Recurrence: How to end the recurrence.<br>
  * End Date: Give the end date to complete the recurrence of the given data.<br>
  * Numbered Occurrences: End recurrence meeting after these many recurrences.


### Schedule Monthly Recurrence Meeting:

Set the following mandatory parameters and other parameters are filled according to need:


* Start Date: Start date of recurrence.<br>
* Recurrence: Monthly<br>
* Recurrence Interval: Interval between months.<br>
  For example: 1 for all months, 2 for every other month.
* Recurrence Type: Type of Recurrence.<br>
  * Absolute Recurrence: To occur on the exact date. Day of month parameter is compulsory. <br>
    For example: If the start date is 21 June 2023, Wednesday then recurrence will occur on the 21st day of the month.<br>
  * Relative Recurrence: To occur on the exact day. Repeat Day and Repeat Index parameter is compulsory.
    For example: If the start date is 21 June 2023, Wednesday then recurrence will occur on Wednesday of the month.
* Repeat Month: Specifies on which specific month you want to repeat the event.
* End Recurrence: How to end the recurrence.<br>
  * End Date: Give the end date to complete the recurrence of the given data.<br>
  * Numbered Occurrences: End recurrence meeting after these many recurrences.


### Schedule Yearly Recurrence Meeting:

Set the following mandatory parameters and other parameters are filled according to need:


* Start Date: Start date of recurrence.<br>
* Recurrence: Yearly<br>
* Recurrence Interval: Interval between Yearly.<br>
  For example: 1 for all yearly, 2 for every other year.
* Recurrence Type: Type of Recurrence.<br>
  * Absolute Recurrence: To occur on the exact date. Day of month parameter is compulsory.<br>
    For example: If the start date is 21 June 2023, Wednesday then recurrence will occur on the 21st day of the month.<br>
  * Relative Recurrence: To occur on the exact day. Repeat Day and Repeat Index parameter is compulsory.<br>
    For example: If the start date is 21 June 2023, Wednesday then recurrence will occur on Wednesday of the month.
* End Recurrence: How to end the recurrence<br>
  * End Date: Give end date to complete recurrence on the given data<br>
  *  Numbered Occurrences: End recurrence meeting after these many recurrences.

### Supported Time Zones For Get Free Slots and Get Schedules:

The Time Zone input parameter for the **Get Free Slots** and **Get Schedules** requests accepts the following time zone
values:

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












