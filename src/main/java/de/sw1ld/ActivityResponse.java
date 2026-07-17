package de.sw1ld;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    String displayName,
    LocalDate date,
    String distance,
    String duration,
    String elapsedTime,
    String avgSpeed,
    String maxSpeed,
    String temperature,
    String totalAscent,
    Integer rate,
    Bike bike,
    List<Position> positions) {

  public ActivityResponse(Activity fd) {
    this(
        fd.id(),
        toDisplayName(fd.name()),
        fd.date(),
        Prettyfier.distanceWithUnit(fd.distance()),
        Prettyfier.duration(fd.duration()),
        Prettyfier.duration(fd.elapsedTime()),
        Prettyfier.speedWithUnit(fd.avgSpeed()),
        Prettyfier.speedWithUnit(fd.maxSpeed()),
        Prettyfier.temperatureWithUnit(fd.temperature()),
        Prettyfier.withMeter(fd.totalAscent()),
        fd.rate(),
        fd.bike(),
        fd.positions());
  }

  private static String toDisplayName(String filename) {
    filename = removeDatePattern(filename);
    filename = removeSuffix(filename);

    filename = filename.replace("_", " ");
    return filename;
  }

  private static String removeDatePattern(String filename) {
    return filename.replaceFirst("^\\d{4}-\\d{2}-\\d{2}_", "");
  }

  private static String removeSuffix(String filename) {
    if (filename.endsWith(".fit")) {
      return filename.substring(0, filename.length() - 4);
    } else {
      return filename;
    }
  }
}
