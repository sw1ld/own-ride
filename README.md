# fit-tooling

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

- [ ] any possibility to add additional tags for filtering (such as by bike, by route, ...)?
- [ ] init drop down for year-filter by "minimal year" of uploaded files
- [ ] bug: small browser window does not scale up charts properly
- [ ] move code to dedicated js files
  - [ ] routeLengthChart 
  - [ ] insert JSON to js files
- [ ] speed over time -> graph
  - [ ] add grade over time in different color?
- [ ] optimization: cache parsed results   
- [ ] upload files via API?
- [ ] persist files?
