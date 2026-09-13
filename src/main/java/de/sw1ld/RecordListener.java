package de.sw1ld;

import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import java.util.ArrayList;
import java.util.List;

public class RecordListener implements RecordMesgListener {
  private double maxSpeed = 0.0;
  private List<Position> positions = new ArrayList<>();

  @Override
  public void onMesg(RecordMesg m) {
    if (m.getSpeed() != null && m.getSpeed() < 25) { // avoid weird peeks (25 m/s -> ca 90km/h)
      maxSpeed = Math.max(maxSpeed, m.getSpeed());
    }
    if (m.getPositionLat() != null && m.getPositionLong() != null) {
      positions.add(
          Position.fromSemicircles(
              m.getPositionLat(),
              m.getPositionLong(),
              m.getEnhancedAltitude() == null
                  ? 0.0f // gets interpolated for smaller spikes
                  : m.getEnhancedAltitude()));
    }
  }

  List<Position> getPositions() {
    return positions;
  }

  /* kilometer per hour */
  double getMaxSpeed() {
    return maxSpeed * 3.6;
  }
}
