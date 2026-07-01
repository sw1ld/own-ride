package de.sw1ld;

import com.garmin.fit.Decode;
import com.garmin.fit.MesgBroadcaster;
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

@ApplicationScoped
public class ActivityService {

  @PersistenceContext EntityManager em;
  private final ActivityDataRepository activityDataRepository;

  public ActivityService(ActivityDataRepository activityDataRepository) {
    this.activityDataRepository = activityDataRepository;
  }

  Optional<Activity> fetchActivityBy(UUID id) {
    return activityDataRepository.findById(id).map(Activity::new);
  }

  List<Activity> fetchActivities(@Nullable Integer year) {
    if (year == null) {
      return activityDataRepository.findAll().stream().map(Activity::new).toList();
    } else {
      return activityDataRepository.findByYear(year).stream().map(Activity::new).toList();
    }
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
      data.setAvgSpeed(ses.getAverageSpeed());
      data.setMaxSpeed(rec.getMaxSpeed());
      data.setTemperature(ses.getTemperature());
      data.setTotalAscent(ses.getTotalAscent());
      data.setTimeCreated(fileId.getTimeCreated());
      data.setLastModified(LocalDateTime.now());
      data.setRate(0);
      data.setPositions(rec.getPositions());

      em.persist(activityRaw);
      em.persist(data);

      return id;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Transactional
  Activity setUserRating(UUID id, Rate rate) {
    ActivityData data =
        activityDataRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Activity data not found"));

    Integer existingRate = data.getRate();

    if (existingRate != null && existingRate.equals(rate.value())) {
      data.setRate(0);
    } else {
      data.setRate(rate.value());
    }

    em.merge(data);

    return new Activity(data);
  }

  @Transactional
  Activity recalculateActivity(UUID id) {
    ActivityData data =
        activityDataRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Activity data not found"));

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

      em.merge(data);
      return new Activity(data);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
