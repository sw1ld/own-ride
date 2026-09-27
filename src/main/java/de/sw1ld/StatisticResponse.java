package de.sw1ld;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record StatisticResponse(
    int rides,
    String distance,
    String ascent,
    String duration,
    String longestDistance,
    UUID longestDistanceActivityId,
    String topSpeed,
    UUID topSpeedActivityId,
    String fastestRide,
    UUID fastestRideActivityId,
    String biggestClimb,
    UUID biggestClimbActivityId,
    Map<LocalDate, Double> tourDates,
    LocalDate start,
    LocalDate end,
    boolean hasPrevious,
    boolean hasNext) {

  public StatisticResponse(
      int rides,
      double distance,
      int ascent,
      Duration duration,
      Highlights highlights,
      Map<LocalDate, Double> tourDates,
      LocalDate start,
      LocalDate end,
      boolean hasPrevious,
      boolean hasNext) {
    this(
        rides,
        Prettyfier.distanceCompactWithUnit(distance),
        Prettyfier.withMeter(ascent),
        Prettyfier.duration(duration),
        Prettyfier.distanceCompactWithUnit(highlights.longestDistance()),
        highlights.longestDistanceActivityId(),
        Prettyfier.speedWithUnit(highlights.topSpeed()),
        highlights.topSpeedActivityId(),
        Prettyfier.speedWithUnit(highlights.fastestRide()),
        highlights.fastestRideActivityId(),
        Prettyfier.withMeter(highlights.biggestClimb()),
        highlights.biggestClimbActivityId(),
        tourDates,
        start,
        end,
        hasPrevious,
        hasNext);
  }
}
