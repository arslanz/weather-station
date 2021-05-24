# weather-station
This is a solution to a coding exercise to produce a REST API based app to handle the serverside CRUD operations of a weather station which can make REST API calls.

The following is an overview of the REST endpoints the solution must expose.
Method  Path                      Request Body         Response Body
POST    /measurements             Measurement          (none)
GET     /measurements/:timestamp  (none)               Measurement
GET     /measurements/:date       (none)               Measurement[]
PUT     /measurements/:timestamp  Measurement          (none)
PATCH   /measurements/:timestamp  Measurement(partial) (none)
DELETE  /measurements/:timestamp  (none)               (none)
GET     /stats1                   (none)               Statistic[]

The /stats endpoint accepts query parameters to for its response. These parameters are:
Parameter     Indicates                                     Notes
stat          which statistic to compute                    can be repeated for more than one statistic
metric        which metric to compute the statistics for    can be repeated for more than one metric
fromDateTime  the inclusive minimum date and time of the    in UTC, ISO-8061 format range
toDateTime    the exclusive maximum date and time of the    in UTC, ISO-8061 format range

## Compiling and running
mvn clean package
mvn exec:java
