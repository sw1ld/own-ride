package de.sw1ld;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatsService {

  private StatsService() {}

  static StatisticResponse getStats(List<FitData> fitData, Integer year) {
    double total = fitData.stream().map(FitData::distance).mapToDouble(Double::doubleValue).sum();

    Map<LocalDate, Double> basicTourStatistics = new TreeMap<>();
    LocalDate start = LocalDate.of(year, 1, 1);
    LocalDate end = LocalDate.of(year, 12, 31);

    // init whole year
    for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
      basicTourStatistics.put(d, 0.0);
    }

    // set/overwrite with concrete values
    fitData.forEach(f -> basicTourStatistics.put(f.date(), f.distance()));

    return new StatisticResponse(fitData.size(), total, basicTourStatistics);
  }
}
