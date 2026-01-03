package de.sw1ld;

import java.time.LocalDate;
import java.util.Map;

public record StatisticResponse(int rides, String distance, Map<LocalDate, Double> tourDates) {

  public StatisticResponse(int rides, double distance, Map<LocalDate, Double> tourDates) {
    this(rides, Prettyfier.distanceWithUnit(distance), tourDates);
  }
}
