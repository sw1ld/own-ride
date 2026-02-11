# fit-tooling

## Preconditions

Add an `.env` file and define a datasource.
Here is an example for a simple (local) postgres datasource. 
```
_DEV_QUARKUS_DATASOURCE_DB_KIND=postgresql
_DEV_QUARKUS_DATASOURCE_USERNAME=someuser
_DEV_QUARKUS_DATASOURCE_PASSWORD=somepassword
_DEV_QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/fitdb
```

## Running the application

```shell script
./mvn quarkus:dev
```

```shell
# Request all data
$ curl http://localhost:8080/fit/details 

# Request aggregated statistics
$ curl http://localhost:8080/fit/stats?year=2025

# Call website
$ curl http://localhost:8080/fit/stats -H 'accept: text/html' 
```

## Open TODOs

- [x] allow possibility to upload all files in a folder
- [ ] make sure to not upload the same file twice (id field in fit file?)
- [ ] any possibility to add additional tags for filtering (such as by bike, by route, ...)?
  - [ ] add simple database to persist "meta data"
- [ ] more details: 
  - [ ] altitude profile (only possible by calculating via GPS positions?)
  - [ ] speed over time -> graph
    - [ ] add grade (Anstieg) over time in different color?
  - [ ] allow to recalculate entries in ActivityData (FE-click?)

- [x] init drop down for year-filter by "minimal year" of uploaded files
- [x] bug: small browser window does not scale up charts properly
- [ ] optimization 
  - [ ] cache parsed results?
  - [ ] parse relevant data only once and persist in database?
- [x] upload files via index page?
- [x] persist files?
  - [x] write stateful test
  - [x] upload via FE
  - [x] show proper result after upload - success message / reason of failure
  - [x] make FE look nicer
  - [x] do not show Activity per year for upload page!
