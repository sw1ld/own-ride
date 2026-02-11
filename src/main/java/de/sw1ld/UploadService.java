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
import java.util.UUID;

@ApplicationScoped
public class UploadService {

  @PersistenceContext EntityManager em;

  @Transactional
  UUID persistActivity(String fileName, byte[] content) {
    UUID activityId = UUID.randomUUID();

    ActivityRaw activityRaw = new ActivityRaw();
    activityRaw.setId(activityId);
    activityRaw.setName(fileName);
    activityRaw.setFitFile(content);
    // check if created date was set properly!
    activityRaw.setLastModified(LocalDateTime.now());

    Decode decode = new Decode();
    MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
    RecordListener rec = new RecordListener();
    SessionListener ses = new SessionListener();

    broadcaster.addListener(rec);
    broadcaster.addListener(ses);

    try (InputStream is = new ByteArrayInputStream(content)) {
      if (is == null) {
        return null;
      }

      decode.read(is, broadcaster);

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
      data.setPositions(rec.getPositions());

      em.persist(activityRaw);
      em.persist(data);

      return id;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
