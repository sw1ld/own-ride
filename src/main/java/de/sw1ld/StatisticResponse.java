package de.sw1ld;

public record StatisticResponse(int rides, String distance) {

  public StatisticResponse(int rides, double distance) {
    this(rides, Prettyfier.distanceWithUnit(distance));
  }
}
