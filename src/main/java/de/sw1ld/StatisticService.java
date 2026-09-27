package de.sw1ld;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class StatisticService {

  private StatisticService() {}

  static StatisticResponse getStats(
      List<PerformanceData> performanceData, DateCalculator dateCalculator) {
    double totalDistance =
        performanceData.stream()
            .map(PerformanceData::distance)
            .mapToDouble(Double::doubleValue)
            .sum();

    int totalAscent =
        performanceData.stream()
            .map(PerformanceData::ascent)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

    Duration totalDuration =
        performanceData.stream()
            .map(PerformanceData::duration)
            .reduce(Duration.ZERO, Duration::plus);

    Highlights highlights = Highlights.extract(performanceData);

    Map<LocalDate, Double> basicTourStatistics = new TreeMap<>();
    LocalDate start = dateCalculator.start();
    LocalDate end = dateCalculator.end();

    // init whole year
    for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
      basicTourStatistics.put(d, 0.0);
    }

    // set/merge with concrete values
    performanceData.forEach(f -> basicTourStatistics.merge(f.date(), f.distance(), Double::sum));

    return new StatisticResponse(
        performanceData.size(),
        totalDistance,
        totalAscent,
        totalDuration,
        highlights,
        basicTourStatistics,
        start,
        end,
        dateCalculator.hasPrevious(),
        dateCalculator.hasNext());
  }
}
