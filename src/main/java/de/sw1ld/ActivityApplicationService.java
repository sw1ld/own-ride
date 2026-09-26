package de.sw1ld;

import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class ActivityApplicationService {

  private final ActivityService activityService;
  private final GroupService groupService;

  public ActivityApplicationService(ActivityService activityService, GroupService groupService) {
    this.activityService = activityService;
    this.groupService = groupService;
  }

  ResponseFragment fetchFragment(@Nullable String encodedCursor) {
    Fragment fragment = activityService.fetchActivities(encodedCursor);
    List<Activity> activities = fragment.activities();

    if (activities.isEmpty()) {
      return new ResponseFragment(List.of(), null);
    }

    Set<UUID> groupIds =
        activities.stream()
            .map(Activity::groupId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    List<Group> candidateGroups = groupService.fetchGroupsByIds(groupIds);

    Set<UUID> currentActivityIds =
        activities.stream().map(Activity::id).collect(Collectors.toSet());

    List<Group> groupsToRender =
        candidateGroups.stream()
            .filter(
                group -> {
                  Activity latest = group.latestActivity();
                  return latest != null && currentActivityIds.contains(latest.id());
                })
            .toList();

    List<FeedItem> activityFeeds =
        alignActivitiesAndGroups(activities, groupsToRender, candidateGroups);

    return new ResponseFragment(
        activityFeeds, fragment.nextCursor() != null ? fragment.nextCursor().encode() : null);
  }

  Optional<Activity> fetchActivityBy(UUID id) {
    return activityService.fetchActivityBy(id);
  }

  boolean deleteActivity(UUID id) {
    return activityService.deleteActivity(id);
  }

  Optional<Activity> recalculateActivity(UUID id) {
    return activityService.recalculateActivity(id);
  }

  Optional<Activity> setUserRating(UUID id, Integer rate) {
    return activityService.setUserRating(id, rate);
  }

  Optional<Activity> linkBike(UUID id, UUID bikeId) {
    return activityService.linkBike(id, bikeId);
  }

  Optional<Activity> updateName(UUID id, String name) {
    return activityService.updateName(id, name);
  }

  private List<FeedItem> alignActivitiesAndGroups(
      List<Activity> activities, List<Group> groupsToRender, List<Group> candidateGroups) {
    List<GroupResponse> groupResponses = groupsToRender.stream().map(GroupResponse::new).toList();

    Set<UUID> groupedActivityIds =
        candidateGroups.stream()
            .flatMap(g -> g.activities().stream())
            .map(Activity::id)
            .collect(Collectors.toSet());

    List<ActivityResponse> standaloneActivities =
        activities.stream()
            .filter(a -> a.groupId() == null || !groupedActivityIds.contains(a.id()))
            .map(ActivityResponse::new)
            .toList();

    return Stream.concat(groupResponses.stream(), standaloneActivities.stream())
        .sorted(Comparator.comparing(this::sortDateOf).thenComparing(FeedItem::id).reversed())
        .collect(Collectors.toList());
  }

  private LocalDate sortDateOf(FeedItem item) {
    if (item instanceof GroupResponse gr) {
      return gr.activities().stream()
          .map(a -> LocalDate.parse(a.date()))
          .max(LocalDate::compareTo)
          .orElse(LocalDate.MIN);
    }
    if (item instanceof ActivityResponse ar) {
      return LocalDate.parse(ar.date());
    }
    return LocalDate.MIN;
  }
}
