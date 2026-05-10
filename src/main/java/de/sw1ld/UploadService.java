package de.sw1ld;

import com.garmin.fit.Decode;
import com.garmin.fit.MesgBroadcaster;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UploadService {

  @PersistenceContext EntityManager em;
  private final ActivityDataRepository activityDataRepository;

  public UploadService(ActivityDataRepository activityDataRepository) {
    this.activityDataRepository = activityDataRepository;
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
      data.setTemperature(ses.getTemperature());
      data.setTotalAscent(ses.getTotalAscent());
      data.setTimeCreated(fileId.getTimeCreated());
      data.setLastModified(LocalDateTime.now());
      data.setPositions(rec.getPositions());

      em.persist(activityRaw);
      em.persist(data);

      return id;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Transactional
  FitData recalculateActivity(UUID id) {
    ActivityData activityData =
        activityDataRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Activity data not found"));

    ActivityRaw activityRaw = activityData.getActivity();
    if (activityRaw == null) {
      throw new IllegalStateException("Raw activity data not found for activity: " + id);
    }

    byte[] content = activityRaw.getFitFile();

    Decode decode = new Decode();
    MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
    RecordListener rec = new RecordListener();
    SessionListener ses = new SessionListener();
    FileIdListener fileId = new FileIdListener();

    broadcaster.addListener(rec);
    broadcaster.addListener(ses);
    broadcaster.addListener(fileId);

    try (InputStream is = new ByteArrayInputStream(content)) {
      decode.read(is, broadcaster);

      activityData.setDate(rec.getDate());
      activityData.setDistance(ses.getDistance());
      activityData.setDuration(ses.getTimeWithoutBreaks());
      activityData.setAvgSpeed(ses.getAverageSpeed());
      activityData.setTemperature(ses.getTemperature());
      activityData.setTotalAscent(ses.getTotalAscent());
      activityData.setTimeCreated(fileId.getTimeCreated());
      activityData.setLastModified(LocalDateTime.now());
      activityData.setPositions(rec.getPositions());

      em.merge(activityData);
      return new FitData(activityData);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
