# fit-tooling

## Running the application

```shell script
./mvn quarkus:dev
```

```shell
# Request all data
$ curl http://localhost:8080/fit/details 

# Request aggregated statistics
$ curl http://localhost:8080/fit/stats

# Call website
$ curl http://localhost:8080/fit/stats -H 'accept: text/html' 
```

## Open TODOs

- [x] subtract pauses from elapsed time (and avg speed)
- [ ] show overall statistics -> dedicated endpoint?
  - [ ] histogram tour length
  - [ ] driven kilometers over time (histogram?) 
  - [ ] enable filtering (for year)
  - [ ] any possibility to add additional tags for filtering (such as by bike, by route, ...)?
- [ ] speed over time -> graph
  - [ ] add grade over time in different color?
- [ ] optimization: cache parsed results   
- [ ] upload files via API?
- [ ] persist files?
