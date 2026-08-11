package de.sw1ld;

import de.sw1ld.thumbnails.ThumbnailService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class GroupService {

  @PersistenceContext EntityManager em;
  private final GroupDataRepository groupRepository;
  private final ActivityDataRepository activityRepository;
  private final ThumbnailService thumbnailService;

  public GroupService(
      GroupDataRepository groupRepository,
      ActivityDataRepository activityRepository,
      ThumbnailService thumbnailService) {
    this.groupRepository = groupRepository;
    this.activityRepository = activityRepository;
    this.thumbnailService = thumbnailService;
  }

  public Optional<Group> fetchGroupBy(UUID id) {
    return groupRepository.findById(id).map(Group::new);
  }

  List<Group> fetchGroups(LocalDate start, LocalDate end) {
    return groupRepository.findInRange(start, end).stream().map(Group::new).toList();
  }

  @Transactional
  public Group createGroup(Set<UUID> activityIds) {
    List<ActivityData> activities = activityRepository.findByIds(activityIds);

    GroupData groupData = new GroupData();
    groupData.setId(UUID.randomUUID());
    groupData.setName("New Group");
    int summedUpRate =
        activities.stream().mapToInt(a -> a.getRate() == null ? 0 : a.getRate()).sum();
    groupData.setRate(summedUpRate == 0 ? 0 : summedUpRate / activities.size());
    groupData.setThumbnail(
        thumbnailService.renderSvg(
            activities.stream().flatMap(a -> a.getPositions().stream()).toList()));
    groupData.setActivities(activities);

    groupRepository.persist(groupData);

    return new Group(groupData);
  }

  @Transactional
  public Optional<Group> addActivityToGroup(UUID groupId, UUID activityId) {
    Optional<GroupData> groupOpt = groupRepository.findById(groupId);
    Optional<ActivityData> activityOpt = activityRepository.findById(activityId);

    if (groupOpt.isEmpty()) return Optional.empty();

    if (activityOpt.isEmpty()) {
      throw new GroupingException("Activity does not exist. Id: " + activityId);
    }

    GroupData groupData = groupOpt.get();
    ActivityData candidate = activityOpt.get();

    List<ActivityData> newGroup = new ArrayList<>(groupData.getActivities());
    newGroup.add(candidate);
    newGroup.sort(Comparator.comparing(ActivityData::getDate));

    groupData.setThumbnail(
        thumbnailService.renderSvg(
            newGroup.stream().flatMap(a -> a.getPositions().stream()).toList()));
    groupData.setActivities(newGroup);

    groupRepository.persist(groupData);

    return Optional.of(new Group(groupData));
  }

  @Transactional
  public Optional<Boolean> removeActivityFromGroup(UUID groupId, UUID activityId) {
    Optional<GroupData> groupOpt = groupRepository.findById(groupId);
    if (groupOpt.isEmpty()) return Optional.empty();

    Optional<ActivityData> activityOpt = activityRepository.findById(activityId);
    if (activityOpt.isEmpty())
      return Optional.of(false); // Group still exists or activity not in group

    ActivityData activityToRemove = activityOpt.get();
    GroupData group = groupOpt.get();
    List<ActivityData> activities = group.getActivities();
    activities.remove(activityToRemove);
    group.setActivities(activities);

    group.setThumbnail(
        thumbnailService.renderSvg(
            activities.stream().flatMap(a -> a.getPositions().stream()).toList()));
    groupRepository.persist(group);

    // If only one activity remains, dissolve the group
    if (group.getActivities().size() < 2) {
      deleteGroup(groupId);
      return Optional.of(true); // Group completely dissolved
    }

    return Optional.of(false);
  }

  @Transactional
  public Optional<Group> updateName(UUID id, String name) {
    return groupRepository
        .findById(id)
        .map(
            g -> {
              g.setName(name);
              return new Group(g);
            });
  }

  @Transactional
  Optional<Group> setUserRating(UUID id, Integer unvalidatedRate) {
    Optional<GroupData> groupData = groupRepository.findById(id);
    if (groupData.isEmpty()) {
      return Optional.empty();
    }
    Rate rate = new Rate(unvalidatedRate);

    GroupData data = groupData.get();
    Integer existingRate = data.getRate();

    if (existingRate != null && existingRate.equals(rate.value())) {
      data.setRate(0);
    } else {
      data.setRate(rate.value());
    }

    em.merge(data);

    return Optional.of(new Group(data));
  }

  @Transactional
  public void deleteGroup(UUID id) {
    groupRepository.findById(id).ifPresent(g -> groupRepository.delete(id));
  }
}
