package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatisticServiceTest {

  @Test
  void sumStatsForSameDate() {
    int year = 2025;
    LocalDate date = LocalDate.of(year, 6, 7);
    PerformanceData ride1 = new PerformanceData(date, 10.5, 100);
    PerformanceData ride2 = new PerformanceData(date, 15.0, 150);

    List<PerformanceData> activities = List.of(ride1, ride2);

    StatisticResponse stats = StatisticService.getStats(activities, year);

    assertThat(stats.rides()).isEqualTo(2);
    assertThat(stats.distance()).isEqualTo("25.50 km");
    assertThat(stats.tourDates()).containsEntry(date, 25.5);
  }
}
