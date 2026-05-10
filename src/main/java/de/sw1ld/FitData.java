package de.sw1ld;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FitData(
    UUID id,
    String name,
    LocalDate date,
    double distance,
    Duration duration,
    double avgSpeed,
    Integer temperature,
    Integer totalAscent,
    LocalDateTime lastModified,
    List<Position> positions) {

  public FitData(ActivityData data) {
    this(
        data.getId(),
        data.getName(),
        data.getDate(),
        data.getDistance(),
        data.getDuration(),
        data.getAvgSpeed(),
        data.getTemperature(),
        data.getTotalAscent(),
        data.getLastModified(),
        data.getPositions());
  }
}
