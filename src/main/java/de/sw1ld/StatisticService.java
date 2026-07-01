package de.sw1ld;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatisticService {

  private StatisticService() {}

  static StatisticResponse getStats(List<Activity> activities, Integer year) {
    double total =
        activities.stream().map(Activity::distance).mapToDouble(Double::doubleValue).sum();

    Map<LocalDate, Double> basicTourStatistics = new TreeMap<>();
    LocalDate start = LocalDate.of(year, 1, 1);
    LocalDate end = LocalDate.of(year, 12, 31);

    // init whole year
    for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
      basicTourStatistics.put(d, 0.0);
    }

    // set/merge with concrete values
    activities.forEach(f -> basicTourStatistics.merge(f.date(), f.distance(), Double::sum));

    return new StatisticResponse(activities.size(), total, basicTourStatistics);
  }
}
