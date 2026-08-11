package de.sw1ld;

import java.util.List;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    FeedType type,
    String displayName,
    String date,
    String distance,
    String duration,
    String elapsedTime,
    String avgSpeed,
    String maxSpeed,
    String temperature,
    String totalAscent,
    Integer rate,
    Bike bike,
    UUID groupId,
    String thumbnail,
    List<Position> positions)
    implements FeedItem {

  public ActivityResponse(Activity a) {
    this(
        a.id(),
        FeedType.ACTIVITY,
        toDisplayName(a.name()),
        a.date().toString(),
        Prettyfier.distanceWithUnit(a.distance()),
        Prettyfier.duration(a.duration()),
        Prettyfier.duration(a.elapsedTime()),
        Prettyfier.speedWithUnit(a.avgSpeed()),
        Prettyfier.speedWithUnit(a.maxSpeed()),
        Prettyfier.temperatureWithUnit(a.temperature()),
        Prettyfier.withMeter(a.totalAscent()),
        a.rate(),
        a.bike(),
        a.groupId(),
        a.thumbnail(),
        a.positions());
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
