package de.sw1ld;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FitResponse(
    UUID id,
    String displayName,
    String name, // used as ID right now
    LocalDate date,
    String distance,
    String duration,
    String avgSpeed,
    String temperature,
    String totalAscent,
    LocalDateTime lastModified,
    List<Position> positions) {

  public FitResponse(FitData fd) {
    this(
        fd.id(),
        toDisplayName(fd.name()),
        fd.name(),
        fd.date(),
        Prettyfier.distanceWithUnit(fd.distance()),
        Prettyfier.duration(fd.duration()),
        Prettyfier.speedWithUnit(fd.avgSpeed()),
        Prettyfier.temperatureWithUnit(fd.temperature()),
        Prettyfier.withMeter(fd.totalAscent()),
        fd.lastModified(),
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
