package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StatsServiceTest {

  @Test
  void sumStatsForSameDate() {
    int year = 2025;
    LocalDate date = LocalDate.of(year, 6, 7);
    LocalDateTime now = LocalDateTime.now();
    FitData ride1 =
        new FitData(
            UUID.randomUUID(),
            "Hinfahrt",
            date,
            10.5,
            Duration.ofHours(1),
            10.5,
            20,
            100,
            now,
            0,
            List.of());
    FitData ride2 =
        new FitData(
            UUID.randomUUID(),
            "Rückfahrt",
            date,
            15.0,
            Duration.ofHours(1),
            15.0,
            22,
            150,
            now,
            0,
            List.of());

    List<FitData> fitData = List.of(ride1, ride2);

    StatisticResponse stats = StatsService.getStats(fitData, year);

    assertThat(stats.rides()).isEqualTo(2);
    assertThat(stats.distance()).isEqualTo("25.50 km");
    assertThat(stats.tourDates()).containsEntry(date, 25.5);
  }
}
