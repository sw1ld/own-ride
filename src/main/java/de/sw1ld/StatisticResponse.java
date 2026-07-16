package de.sw1ld;

import java.time.LocalDate;
import java.util.Map;

public record StatisticResponse(
    int rides, String distance, String ascent, Map<LocalDate, Double> tourDates) {

  public StatisticResponse(
      int rides, double distance, int ascent, Map<LocalDate, Double> tourDates) {
    this(rides, Prettyfier.distanceWithUnit(distance), Prettyfier.withMeter(ascent), tourDates);
  }
}
