package de.sw1ld;

import java.time.Duration;

public class Prettyfier {

  private Prettyfier() {}

  static String distanceCompactWithUnit(double distance) {
    if (distance >= 1000.0) {
      return "%.2fK km".formatted(distance / 1000);
    }
    return distanceWithUnit(distance);
  }

  static String distanceWithUnit(double distance) {
    return "%.2f km".formatted(distance);
  }

  static String speedWithUnit(double speed) {
    return "%.2f km/h".formatted(speed);
  }

  static String temperatureWithUnit(Integer temp) {
    if (temp == null) {
      return "/";
    } else {
      return "%d °C".formatted(temp);
    }
  }

  public static String duration(Duration d) {
    if (d == null) {
      return "not defined";
    }

    if (d.toDays() >= 1) {
      double totalHours = d.toSeconds() / 3_600.0;
      return "%.2f h".formatted(totalHours);
    }

    return "%02d:%02d:%02d h".formatted(d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart());
  }

  public static String withMeter(Integer value) {
    if (value == null) {
      return "/";
    }
    if (value >= 10000) {
      return "%3dK m".formatted(value / 1000);
    } else {
      return "%d m".formatted(value);
    }
  }
}
