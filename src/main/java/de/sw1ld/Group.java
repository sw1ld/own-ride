package de.sw1ld;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Group(
    UUID id, String name, Integer rate, String thumbnail, List<Activity> activities) {

  public Group(GroupData data) {
    this(
        data.getId(),
        data.getName(),
        data.getRate(),
        data.getThumbnail(),
        data.getActivities().stream()
            .map(Activity::new)
            .sorted(Comparator.comparing(Activity::date))
            .toList());
  }

  public double totalDistance() {
    return activities.stream().mapToDouble(Activity::distance).sum();
  }

  public Duration totalDuration() {
    return activities.stream().map(Activity::duration).reduce(Duration.ZERO, Duration::plus);
  }

  public Duration totalElapsedTime() {
    return activities.stream().map(Activity::elapsedTime).reduce(Duration.ZERO, Duration::plus);
  }

  public double avgSpeed() {
    double totalDist = totalDistance();
    long totalSec = totalDuration().getSeconds();
    if (totalSec == 0) return 0;
    return (totalDist / totalSec) * 3600;
  }

  public int totalAscent() {
    return activities.stream().mapToInt(a -> a.totalAscent() != null ? a.totalAscent() : 0).sum();
  }

  public LocalDate startDate() {
    return activities.stream()
        .map(Activity::date)
        .min(LocalDate::compareTo)
        .orElse(LocalDate.now());
  }

  public LocalDate endDate() {
    return activities.stream()
        .map(Activity::date)
        .max(LocalDate::compareTo)
        .orElse(LocalDate.now());
  }

  public List<Position> positions() {
    return activities.stream().flatMap(a -> a.positions().stream()).toList();
  }

  public Integer avgTemperature() {
    List<Integer> temps =
        activities.stream().map(Activity::temperature).filter(Objects::nonNull).toList();
    if (temps.isEmpty()) return null;
    return (int) Math.round(temps.stream().mapToInt(Integer::intValue).average().orElse(0));
  }

  public Bike usedBike() {
    List<Bike> bikes =
        activities.stream().map(Activity::bike).filter(Objects::nonNull).distinct().toList();
    if (bikes.size() == 1) {
      return bikes.getFirst();
    }
    return null;
  }
}
