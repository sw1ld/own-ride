package de.sw1ld;

import java.time.LocalDate;
import java.util.List;

public record FitData(
    String name, LocalDate date, double distance, double avgSpeed, List<Position> positions) {}
