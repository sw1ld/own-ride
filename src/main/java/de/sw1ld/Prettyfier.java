package de.sw1ld;

import java.time.Duration;

public class Prettyfier {

  private Prettyfier() {}

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

  public static String duration(Duration duration) {
    if (duration == null) {
      return "not defined";
    }
    return "%d:%02d:%02d"
        .formatted(duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
  }

  public static String withMeter(Integer value) {
    if (value == null) {
      return "/";
    } else {
      return "%d m".formatted(value);
    }
  }
}
