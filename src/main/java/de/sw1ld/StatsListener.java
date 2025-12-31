package de.sw1ld;

import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatsListener implements RecordMesgListener {
  private double lastDistance = 0;
  private double speedSum = 0;
  private int count = 0;
  private List<Position> positions = new ArrayList<>();

  @Override
  public void onMesg(RecordMesg m) {
    if (m.getDistance() != null) {
      lastDistance = m.getDistance();
    }
    if (m.getSpeed() != null) {
      speedSum += m.getSpeed();
      count++;
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

  /* km */
  double getTotalDistance() {
    return lastDistance / 1000.0;
  }

  /* kilometer per hour */
  double getAverageSpeed() {
    return count > 0 ? (speedSum / count * 3.6) : 0;
  }
}
