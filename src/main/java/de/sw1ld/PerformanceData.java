package de.sw1ld;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

public record PerformanceData(
    UUID id,
    LocalDate date,
    Double distance,
    Integer ascent,
    Duration duration,
    Double avgSpeed,
    Double maxSpeed) {}
