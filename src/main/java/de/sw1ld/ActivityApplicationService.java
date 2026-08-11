package de.sw1ld;

import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

    List<Group> groups = List.of();
    if (!activities.isEmpty()) {
      groups = groupService.fetchGroups(activities.getLast().date(), activities.getFirst().date());
      if (encodedCursor != null && !groups.isEmpty()) {
        Group isCompleteGroup = groups.getFirst();
        if (!new HashSet<>(activities).containsAll(isCompleteGroup.activities())) {
          groups = new ArrayList<>(groups);
          groups.remove(isCompleteGroup);

          // make sure that groups (or activities in groups) get displayed twice
          activities = new ArrayList<>(activities);
          activities.removeAll(isCompleteGroup.activities());
        }
      }
    }

    List<FeedItem> activityFeeds = alignActivitiesAndGroups(activities, groups);

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

  private List<FeedItem> alignActivitiesAndGroups(List<Activity> activities, List<Group> groups) {
    List<GroupResponse> groupedActivities = groups.stream().map(GroupResponse::new).toList();

    Set<ActivityResponse> collect =
        groupedActivities.stream()
            .flatMap(a -> a.activities().stream())
            .collect(Collectors.toSet());

    List<ActivityResponse> standaloneActivities =
        activities.stream().map(ActivityResponse::new).filter(a -> !collect.contains(a)).toList();

    return Stream.concat(groupedActivities.stream(), standaloneActivities.stream())
        .sorted(Comparator.comparing(FeedItem::date).reversed())
        .collect(Collectors.toList());
  }
}
