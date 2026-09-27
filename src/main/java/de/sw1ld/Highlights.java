package de.sw1ld;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public record Highlights(
    double longestDistance,
    UUID longestDistanceActivityId,
    double topSpeed,
    UUID topSpeedActivityId,
    double fastestRide,
    UUID fastestRideActivityId,
    int biggestClimb,
    UUID biggestClimbActivityId) {

  static @NonNull Highlights extract(List<PerformanceData> performanceData) {
    Optional<PerformanceData> longestDistanceRide =
        performanceData.stream().max(Comparator.comparingDouble(PerformanceData::distance));
    double longestDistance = longestDistanceRide.map(PerformanceData::distance).orElse(0.0);
    UUID longestDistanceActivityId = longestDistanceRide.map(PerformanceData::id).orElse(null);

    Optional<PerformanceData> topSpeedRide =
        performanceData.stream().max(Comparator.comparingDouble(PerformanceData::maxSpeed));
    double topSpeed = topSpeedRide.map(PerformanceData::maxSpeed).orElse(0.0);
    UUID topSpeedActivityId = topSpeedRide.map(PerformanceData::id).orElse(null);

    Optional<PerformanceData> fastestRideData =
        performanceData.stream().max(Comparator.comparingDouble(PerformanceData::avgSpeed));
    double fastestRide = fastestRideData.map(PerformanceData::avgSpeed).orElse(0.0);
    UUID fastestRideActivityId = fastestRideData.map(PerformanceData::id).orElse(null);

    Optional<PerformanceData> biggestClimbRide =
        performanceData.stream()
            .filter(f -> f.ascent() != null)
            .max(Comparator.comparingInt(PerformanceData::ascent));
    int biggestClimb = biggestClimbRide.map(PerformanceData::ascent).orElse(0);
    UUID biggestClimbActivityId = biggestClimbRide.map(PerformanceData::id).orElse(null);

    return new Highlights(
        longestDistance,
        longestDistanceActivityId,
        topSpeed,
        topSpeedActivityId,
        fastestRide,
        fastestRideActivityId,
        biggestClimb,
        biggestClimbActivityId);
  }
}
