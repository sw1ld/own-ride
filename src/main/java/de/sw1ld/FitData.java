package de.sw1ld;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public record FitData(
    String name,
    LocalDate date,
    double distance,
    Duration duration,
    double avgSpeed,
    double maxSpeed,
    Integer totalAscent,
    List<Position> positions) {}
