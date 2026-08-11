package de.sw1ld;

import com.garmin.fit.Decode;
import com.garmin.fit.MesgBroadcaster;
import de.sw1ld.thumbnails.ThumbnailService;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.jspecify.annotations.NonNull;

@ApplicationScoped
public class ActivityService {

  private static final int FEED_LIMIT = 20;
  @PersistenceContext EntityManager em;
  private final ActivityDataRepository activityDataRepository;
  private final ThumbnailService thumbnailService;

  public ActivityService(
      ActivityDataRepository activityDataRepository, ThumbnailService thumbnailService) {
    this.activityDataRepository = activityDataRepository;
    this.thumbnailService = thumbnailService;
  }

  Optional<Activity> fetchActivityBy(UUID id) {
    return activityDataRepository.findById(id).map(Activity::new);
  }

  Fragment fetchActivities(@Nullable String encodedCursor) {
    Cursor cursor = Cursor.decode(encodedCursor);

    List<Activity> items =
        activityDataRepository.fetchFeed(cursor, FEED_LIMIT).stream().map(Activity::new).toList();

    boolean hasMore = items.size() > FEED_LIMIT;
    Cursor nextCursor = null;
    if (hasMore) {
      items = items.subList(0, FEED_LIMIT);
      nextCursor = new Cursor(items.getLast().date(), items.getLast().id());
    }

    return new Fragment(items, nextCursor);
  }

  List<PerformanceData> fetchPerformanceData(@NonNull Integer year) {
    return activityDataRepository.fetchPerformanceDataByYear(year);
  }

  List<Integer> getAvailableYears() {
    int currentYear = LocalDate.now().getYear();
    int minYear = activityDataRepository.findMinYear().orElse(currentYear);

    // We want a descending list (most recent year first)
    return IntStream.rangeClosed(minYear, currentYear).boxed().sorted((a, b) -> b - a).toList();
  }

  @Transactional
  boolean deleteActivity(UUID id) {
    return activityDataRepository.delete(id);
  }

  @Transactional
  UUID persistActivity(String fileName, byte[] content) {
    UUID activityId = UUID.randomUUID();

    ActivityRaw activityRaw = new ActivityRaw();
    activityRaw.setId(activityId);
    activityRaw.setName(fileName);
    activityRaw.setFitFile(content);

    Decode decode = new Decode();
    MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
    RecordListener rec = new RecordListener();
    SessionListener ses = new SessionListener();
    FileIdListener fileId = new FileIdListener();

    broadcaster.addListener(rec);
    broadcaster.addListener(ses);
    broadcaster.addListener(fileId);

    try (InputStream is = new ByteArrayInputStream(content)) {
      if (is == null) {
        return null;
      }

      decode.read(is, broadcaster);

      if (fileId.getTimeCreated() != null) {
        Optional<ActivityData> existingActivity =
            activityDataRepository.findByTimeCreated(fileId.getTimeCreated());

        if (existingActivity.isPresent()) {
          throw new IllegalArgumentException("Activity already uploaded");
        }
      }

      ActivityData data = new ActivityData();
      UUID id = UUID.randomUUID();
      data.setActivity(activityRaw);
      data.setId(id);
      data.setName(fileName);
      data.setDate(rec.getDate());
      data.setDistance(ses.getDistance());
      data.setDuration(ses.getTimeWithoutBreaks());
      data.setElapsedTime(ses.getElapsedTime());
      data.setAvgSpeed(ses.getAverageSpeed());
      data.setMaxSpeed(rec.getMaxSpeed());
      data.setTemperature(ses.getTemperature());
      data.setTotalAscent(ses.getTotalAscent());
      data.setTimeCreated(fileId.getTimeCreated());
      data.setLastModified(LocalDateTime.now());
      data.setRate(0);
      data.setPositions(rec.getPositions());
      data.setThumbnail(thumbnailService.renderSvg(rec.getPositions()));

      em.persist(activityRaw);
      em.persist(data);

      return id;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Transactional
  Optional<Activity> setUserRating(UUID activityId, Integer unvalidatedRate) {
    Optional<ActivityData> activityData = activityDataRepository.findById(activityId);
    if (activityData.isEmpty()) {
      return Optional.empty();
    }
    Rate rate = new Rate(unvalidatedRate);

    ActivityData data = activityData.get();
    Integer existingRate = data.getRate();

    if (existingRate != null && existingRate.equals(rate.value())) {
      data.setRate(0);
    } else {
      data.setRate(rate.value());
    }

    em.merge(data);

    return Optional.of(new Activity(data));
  }

  @Transactional
  Optional<Activity> linkBike(UUID activityId, @Nullable UUID bikeId) {
    Optional<ActivityData> activityData = activityDataRepository.findById(activityId);
    if (activityData.isEmpty()) {
      return Optional.empty();
    }
    ActivityData data = activityData.get();

    if (bikeId == null) {
      data.setBike(null);
    } else {
      BikeData bike = em.find(BikeData.class, bikeId);
      if (bike == null) {
        throw new IllegalArgumentException("Bike not found");
      }
      data.setBike(bike);
    }

    em.merge(data);
    return Optional.of(new Activity(data));
  }

  @Transactional
  Optional<Activity> updateName(UUID activityId, String name) {
    Optional<ActivityData> activityData = activityDataRepository.findById(activityId);
    if (activityData.isEmpty()) {
      return Optional.empty();
    }
    ActivityData data = activityData.get();
    data.setName(name);
    data.setLastModified(LocalDateTime.now());

    em.merge(data);
    return Optional.of(new Activity(data));
  }

  @Transactional
  Optional<Activity> recalculateActivity(UUID activityId) {
    Optional<ActivityData> activityData = activityDataRepository.findById(activityId);
    if (activityData.isEmpty()) {
      return Optional.empty();
    }
    ActivityData data = activityData.get();

    Decode decode = new Decode();
    MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
    RecordListener rec = new RecordListener();
    SessionListener ses = new SessionListener();
    FileIdListener fileId = new FileIdListener();

    broadcaster.addListener(rec);
    broadcaster.addListener(ses);
    broadcaster.addListener(fileId);

    byte[] content = data.getActivity().getFitFile();
    try (InputStream is = new ByteArrayInputStream(content)) {
      decode.read(is, broadcaster);

      data.setDate(rec.getDate());
      data.setDistance(ses.getDistance());
      data.setDuration(ses.getTimeWithoutBreaks());
      data.setElapsedTime(ses.getElapsedTime());
      data.setAvgSpeed(ses.getAverageSpeed());
      data.setMaxSpeed(rec.getMaxSpeed());
      data.setTemperature(ses.getTemperature());
      data.setTotalAscent(ses.getTotalAscent());
      data.setTimeCreated(fileId.getTimeCreated());
      data.setLastModified(LocalDateTime.now());
      data.setPositions(rec.getPositions());
      data.setThumbnail(thumbnailService.renderSvg(rec.getPositions()));

      em.merge(data);
      return Optional.of(new Activity(data));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
