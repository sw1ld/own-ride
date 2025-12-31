package de.sw1ld;

public class ResponseHelper {

  private ResponseHelper() {}

  static String formattedDistanceWithUnit(double distance) {
    return "%.2f km".formatted(distance);
  }

  static String formattedSpeedWithUnit(double speed) {
    return "%.2f km/h".formatted(speed);
  }
}
