package de.sw1ld;

import java.util.List;

public class StatsService {

  private StatsService() {}

  static StatisticResponse getStats(List<FitData> fitData) {
    double total = fitData.stream().map(FitData::distance).mapToDouble(Double::doubleValue).sum();

    return new StatisticResponse(fitData.size(), total);
  }
}
