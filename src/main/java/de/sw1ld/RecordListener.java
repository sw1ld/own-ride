package de.sw1ld;

import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import java.time.LocalDate;
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
    if (m.getPositionLat() != null && m.getPositionLat() != null) {
      positions.add(new Position(m.getPositionLat(), m.getPositionLong(), m.getTimestamp()));
    }
  }

  List<Position> getPositions() {
    return positions;
  }

  LocalDate getDate() {
    if (positions.isEmpty()) {
      return null;
    } else {
      // good enough for me, since I am doing one-day rides only ;-)
      return positions.getFirst().timestamp();
    }
  }

  /* kilometer per hour */
  double getMaxSpeed() {
    return maxSpeed * 3.6;
  }
}
