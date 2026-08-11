package de.sw1ld;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public record GroupResponse(
    UUID id,
    FeedType type,
    String displayName,
    String date,
    String distance,
    String duration,
    String elapsedTime,
    String avgSpeed,
    String temperature,
    String totalAscent,
    Integer rate,
    Bike bike,
    String thumbnail,
    List<Position> positions,
    List<ActivityResponse> activities)
    implements FeedItem {

  public GroupResponse(Group g) {
    this(
        g.id(),
        FeedType.GROUP,
        g.name(),
        getDateRange(g),
        Prettyfier.distanceWithUnit(g.totalDistance()),
        Prettyfier.duration(g.totalDuration()),
        Prettyfier.duration(g.totalElapsedTime()),
        Prettyfier.speedWithUnit(g.avgSpeed()),
        Prettyfier.temperatureWithUnit(g.avgTemperature()),
        Prettyfier.withMeter(g.totalAscent()),
        g.rate(),
        g.usedBike(),
        g.thumbnail(),
        g.positions(),
        g.activities().stream().map(ActivityResponse::new).toList());
  }

  private static @NonNull String getDateRange(Group group) {
    return group.startDate().equals(group.endDate())
        ? group.startDate().toString()
        : group.startDate() + " - " + group.endDate();
  }
}
