package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

class GroupTest {

  @Test
  void groupOfEqualActivities() {
    GroupData mock = mock(GroupData.class);
    ActivityData activity = activity(LocalDate.now(), 50.0, Duration.ofHours(2));
    when(mock.getActivities()).thenReturn(List.of(activity, activity));
    Group cut = new Group(mock);

    assertThat(cut.startDate()).isEqualTo(LocalDate.now());
    assertThat(cut.endDate()).isEqualTo(LocalDate.now());
    assertThat(cut.totalDuration()).isEqualTo(Duration.ofHours(4));
    assertThat(cut.totalDistance()).isEqualTo(100.0);
    assertThat(cut.avgSpeed()).isEqualTo(25.0);
  }

  @Test
  void groupOfDifferentActivities() {
    GroupData mock = mock(GroupData.class);
    ActivityData activity1 =
        activity(LocalDate.of(2026, Month.AUGUST, 12), 50.0, Duration.ofHours(2));
    ActivityData activity2 =
        activity(LocalDate.of(2026, Month.AUGUST, 18), 100.0, Duration.ofHours(5));
    when(mock.getActivities()).thenReturn(List.of(activity1, activity2));
    Group cut = new Group(mock);

    assertThat(cut.startDate()).isEqualTo(LocalDate.of(2026, Month.AUGUST, 12));
    assertThat(cut.endDate()).isEqualTo(LocalDate.of(2026, Month.AUGUST, 18));
    assertThat(cut.totalDuration()).isEqualTo(Duration.ofHours(7));
    assertThat(cut.totalDistance()).isEqualTo(150.0);
    assertThat(cut.avgSpeed()).isCloseTo(21.4, Offset.offset(0.1));
  }

  private static ActivityData activity(LocalDate date, double distance, Duration duration) {
    ActivityData activity = new ActivityData();
    activity.setId(UUID.randomUUID());
    activity.setDate(date);
    activity.setDuration(duration);
    activity.setDistance(distance);
    activity.setAvgSpeed(10.0); // not needed for calculation!
    activity.setPositions(List.of());
    return activity;
  }
}
