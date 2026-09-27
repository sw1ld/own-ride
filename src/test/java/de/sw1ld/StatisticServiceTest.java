package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StatisticServiceTest {

  @Test
  void sumStatsForSameDate() {
    LocalDate date = LocalDate.of(2025, 6, 7);
    UUID ride1Id = UUID.randomUUID();
    UUID ride2Id = UUID.randomUUID();
    PerformanceData ride1 =
        new PerformanceData(ride1Id, date, 10.5, 100, Duration.ofMinutes(60), 30.0, 60.0);
    PerformanceData ride2 =
        new PerformanceData(ride2Id, date, 15.0, 150, Duration.ofMinutes(90), 25.0, 58.0);

    List<PerformanceData> activities = List.of(ride1, ride2);

    StatisticResponse stats =
        StatisticService.getStats(activities, new DateCalculator("ALL", null, date));

    assertThat(stats.rides()).isEqualTo(2);
    assertThat(stats.distance()).isEqualTo("25.50 km");
    assertThat(stats.ascent()).isEqualTo("250 m");
    assertThat(stats.duration()).isEqualTo("02:30:00 h");
    assertThat(stats.topSpeed()).isEqualTo("60.00 km/h");
    assertThat(stats.topSpeedActivityId()).isEqualTo(ride1Id);
    assertThat(stats.fastestRide()).isEqualTo("30.00 km/h");
    assertThat(stats.fastestRideActivityId()).isEqualTo(ride1Id);
    assertThat(stats.longestDistanceActivityId()).isEqualTo(ride2Id);
    assertThat(stats.biggestClimbActivityId()).isEqualTo(ride2Id);
    assertThat(stats.tourDates()).containsEntry(date, 25.5);
  }
}
