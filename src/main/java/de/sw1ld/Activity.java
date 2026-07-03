package de.sw1ld;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Activity(
    UUID id,
    String name, // upload file name
    LocalDate date,
    double distance,
    Duration duration,
    Duration elapsedTime,
    double avgSpeed,
    double maxSpeed,
    Integer temperature,
    Integer totalAscent,
    LocalDateTime lastModified,
    Integer rate,
    List<Position> positions) {

  public Activity(ActivityData data) {
    this(
        data.getId(),
        data.getName(),
        data.getDate(),
        data.getDistance(),
        data.getDuration(),
        data.getElapsedTime(),
        data.getAvgSpeed(),
        data.getMaxSpeed() == null ? 0.0 : data.getMaxSpeed(),
        data.getTemperature(),
        data.getTotalAscent(),
        data.getLastModified(),
        data.getRate(),
        data.getPositions());
  }
}
