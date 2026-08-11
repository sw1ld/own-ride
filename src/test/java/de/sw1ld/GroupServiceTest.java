package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.sw1ld.thumbnails.ThumbnailService;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

  private GroupService cut;

  @Mock private ActivityDataRepository activityDataRepoMock;
  @Mock private GroupDataRepository groupDataRepoMock;

  @BeforeEach
  void setUp() {
    activityDataRepoMock = mock(ActivityDataRepository.class);
    groupDataRepoMock = mock(GroupDataRepository.class);
    cut = new GroupService(groupDataRepoMock, activityDataRepoMock, mock(ThumbnailService.class));
  }

  @Test
  void createGroup_sameDate() {
    LocalDate sameDate = LocalDate.of(2026, Month.AUGUST, 1);

    UUID id1 = UUID.randomUUID();
    ActivityData outbound = activity(id1, sameDate);
    UUID id2 = UUID.randomUUID();
    ActivityData inbound = activity(id2, sameDate);

    when(activityDataRepoMock.findByIds(Set.of(id1, id2))).thenReturn(List.of(outbound, inbound));

    Group result = cut.createGroup(Set.of(id1, id2));

    assertThat(result.startDate()).isEqualTo("2026-08-01");
    assertThat(result.endDate()).isEqualTo("2026-08-01");
    assertThat(result.totalDistance()).isEqualTo(20.0);
  }

  @Test
  void createGroup_multipleDates() {
    LocalDate day1 = LocalDate.of(2026, Month.AUGUST, 1);
    LocalDate day2 = LocalDate.of(2026, Month.AUGUST, 2);

    UUID id1 = UUID.randomUUID();
    ActivityData ride1 = activity(id1, day1);
    UUID id2 = UUID.randomUUID();
    ActivityData ride2 = activity(id2, day2);

    when(activityDataRepoMock.findByIds(Set.of(id1, id2))).thenReturn(List.of(ride1, ride2));

    Group result = cut.createGroup(Set.of(id1, id2));

    assertThat(result.startDate()).isEqualTo("2026-08-01");
    assertThat(result.endDate()).isEqualTo("2026-08-02");
    assertThat(result.totalDistance()).isEqualTo(20.0);
  }

  @Test
  void addActivityToGroup_sameDay() {
    LocalDate day = LocalDate.of(2026, Month.AUGUST, 1);

    UUID groupId = UUID.randomUUID();
    ActivityData ride1 = activity(UUID.randomUUID(), day);
    ActivityData ride2 = activity(UUID.randomUUID(), day);
    List<ActivityData> groupedActivities = List.of(ride1, ride2);

    UUID candidateId = UUID.randomUUID();
    ActivityData candidate = activity(candidateId, day);

    when(groupDataRepoMock.findById(groupId))
        .thenReturn(Optional.of(group(groupId, groupedActivities)));
    when(activityDataRepoMock.findById(candidateId)).thenReturn(Optional.of(candidate));

    Optional<Group> result = cut.addActivityToGroup(groupId, candidateId);

    assertThat(result).isPresent();
    assertThat(result.get().startDate()).isEqualTo("2026-08-01");
    assertThat(result.get().endDate()).isEqualTo("2026-08-01");
    assertThat(result.get().totalDistance()).isEqualTo(30.0);
  }

  @Test
  void addActivityToGroup_earlierDay() {
    LocalDate day1 = LocalDate.of(2026, Month.AUGUST, 1);
    LocalDate day2 = LocalDate.of(2026, Month.AUGUST, 2);
    LocalDate day3 = LocalDate.of(2026, Month.AUGUST, 3);

    UUID groupId = UUID.randomUUID();
    UUID candidateId = UUID.randomUUID();
    ActivityData candidate = activity(candidateId, day1); // not yet in group

    ActivityData ride2 = activity(UUID.randomUUID(), day2);
    ActivityData ride3 = activity(UUID.randomUUID(), day3);
    List<ActivityData> groupedActivities = List.of(ride2, ride3);

    when(groupDataRepoMock.findById(groupId))
        .thenReturn(Optional.of(group(groupId, groupedActivities)));
    when(activityDataRepoMock.findById(candidateId)).thenReturn(Optional.of(candidate));

    Optional<Group> result = cut.addActivityToGroup(groupId, candidateId);

    assertThat(result).isPresent();
    assertThat(result.get().startDate()).isEqualTo("2026-08-01");
    assertThat(result.get().endDate()).isEqualTo("2026-08-03");
    assertThat(result.get().totalDistance()).isEqualTo(30.0);
  }

  private static GroupData group(UUID id, List<ActivityData> activities) {
    GroupData group = new GroupData();
    group.setId(id);
    group.setActivities(activities);
    return group;
  }

  private static ActivityData activity(UUID id, LocalDate date) {
    ActivityData activity = new ActivityData();
    activity.setId(id);
    activity.setDate(date);
    activity.setDistance(10.0);
    activity.setAvgSpeed(25.0);
    activity.setPositions(List.of());
    return activity;
  }
}
