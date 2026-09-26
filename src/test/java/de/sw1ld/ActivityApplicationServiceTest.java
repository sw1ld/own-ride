package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityApplicationServiceTest {

  private ActivityApplicationService cut;

  @Mock private ActivityService activityServiceMock;
  @Mock private GroupService groupServiceMock;

  @BeforeEach
  void setUp() {
    activityServiceMock = mock(ActivityService.class);
    groupServiceMock = mock(GroupService.class);
    cut = new ActivityApplicationService(activityServiceMock, groupServiceMock);
  }

  @Test
  void fetchFragment_emptyList() {
    when(activityServiceMock.fetchActivities(null)).thenReturn(new Fragment(List.of(), null));

    ResponseFragment result = cut.fetchFragment(null);

    assertThat(result.items()).isEmpty();
    assertThat(result.nextEncodedCursor()).isNull();
  }

  @Test
  void fetchFragment_standaloneActivitiesOnly() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    Activity a1 = createActivity(id1, LocalDate.of(2026, 5, 20), null);
    Activity a2 = createActivity(id2, LocalDate.of(2026, 5, 19), null);

    when(activityServiceMock.fetchActivities(null))
        .thenReturn(new Fragment(List.of(a1, a2), new Cursor(a2.date(), a2.id())));

    ResponseFragment result = cut.fetchFragment(null);

    assertThat(result.items()).hasSize(2);
    assertThat(result.items().get(0).id()).isEqualTo(id1);
    assertThat(result.items().get(0).type()).isEqualTo(FeedType.ACTIVITY);
    assertThat(result.items().get(1).id()).isEqualTo(id2);
    assertThat(result.items().get(1).type()).isEqualTo(FeedType.ACTIVITY);
    assertThat(result.nextEncodedCursor()).isNotNull();
  }

  @Test
  void fetchFragment_groupOnSinglePage_deduplicatesGroupActivities() {
    UUID groupId = UUID.randomUUID();
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID standaloneId = UUID.randomUUID();

    Activity a1 = createActivity(id1, LocalDate.of(2026, 5, 20), groupId);
    Activity a2 = createActivity(id2, LocalDate.of(2026, 5, 18), groupId);
    Activity standalone = createActivity(standaloneId, LocalDate.of(2026, 5, 19), null);

    Group group = createGroup(groupId, "Tour", List.of(a1, a2));

    when(activityServiceMock.fetchActivities(null))
        .thenReturn(new Fragment(List.of(a1, standalone, a2), null));
    when(groupServiceMock.fetchGroupsByIds(Set.of(groupId))).thenReturn(List.of(group));

    ResponseFragment result = cut.fetchFragment(null);

    assertThat(result.items()).hasSize(2);
    assertThat(result.items().get(0).id()).isEqualTo(groupId);
    assertThat(result.items().get(0).type()).isEqualTo(FeedType.GROUP);
    assertThat(result.items().get(1).id()).isEqualTo(standaloneId);
    assertThat(result.items().get(1).type()).isEqualTo(FeedType.ACTIVITY);
  }

  @Test
  void fetchFragment_groupSpanningPages_renderedOnFirstPageOnly() {
    UUID groupId = UUID.randomUUID();
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Activity a1 = createActivity(id1, LocalDate.of(2026, 5, 20), groupId);
    Activity a2 = createActivity(id2, LocalDate.of(2026, 5, 10), groupId);
    Group group = createGroup(groupId, "Multi-Day Tour", List.of(a1, a2));

    // Page 1 contains a1
    when(activityServiceMock.fetchActivities(null))
        .thenReturn(new Fragment(List.of(a1), new Cursor(a1.date(), a1.id())));
    when(groupServiceMock.fetchGroupsByIds(Set.of(groupId))).thenReturn(List.of(group));

    ResponseFragment page1Result = cut.fetchFragment(null);

    assertThat(page1Result.items()).hasSize(1);
    assertThat(page1Result.items().getFirst().id()).isEqualTo(groupId);
    assertThat(page1Result.items().getFirst().type()).isEqualTo(FeedType.GROUP);

    // Page 2 contains a2 and a standalone activity
    UUID standaloneId = UUID.randomUUID();
    Activity standalone = createActivity(standaloneId, LocalDate.of(2026, 5, 9), null);
    String cursorStr = new Cursor(a1.date(), a1.id()).encode();

    when(activityServiceMock.fetchActivities(cursorStr))
        .thenReturn(new Fragment(List.of(a2, standalone), null));

    ResponseFragment page2Result = cut.fetchFragment(cursorStr);

    // Group already rendered on Page 1, so Page 2 should ONLY contain the standalone activity
    assertThat(page2Result.items()).hasSize(1);
    assertThat(page2Result.items().getFirst().id()).isEqualTo(standaloneId);
    assertThat(page2Result.items().getFirst().type()).isEqualTo(FeedType.ACTIVITY);
  }

  private static Activity createActivity(UUID id, LocalDate date, UUID groupId) {
    return new Activity(
        id,
        "2026-05-20_Route",
        date,
        50.0,
        Duration.ofHours(2),
        Duration.ofHours(2),
        25.0,
        40.0,
        20,
        100,
        LocalDateTime.now(),
        4,
        null,
        groupId,
        null,
        List.of());
  }

  private static Group createGroup(UUID id, String name, List<Activity> activities) {
    return new Group(id, name, 4, null, activities);
  }
}
