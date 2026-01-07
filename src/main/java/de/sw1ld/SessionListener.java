package de.sw1ld;

import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;
import java.time.Duration;

public class SessionListener implements SessionMesgListener {

  private double distance = 0.0;
  private Duration elapsedTime;
  private Duration timeWithoutBreaks;
  private Integer totalAscent;
  private double averageSpeed = 0.0;
  private Integer temperature;

  @Override
  public void onMesg(SessionMesg mesg) {
    Float totalDistance = mesg.getTotalDistance();
    Float totalTime = mesg.getTotalElapsedTime();
    Float movingTime = mesg.getTotalTimerTime();
    if (totalTime != null) {
      this.elapsedTime = Duration.ofSeconds(totalTime.longValue());
    }
    if (movingTime != null) {
      this.timeWithoutBreaks = Duration.ofSeconds(movingTime.longValue());
    }

    if (totalDistance != null) {
      this.distance = totalDistance / 1000.0; // kilometer
    }

    if (totalDistance != null && movingTime != null && movingTime > 0) {
      double avgSpeedMs = totalDistance / movingTime;

      this.averageSpeed = avgSpeedMs * 3.6; // kilometer per hour
    }

    this.totalAscent = mesg.getTotalAscent();

    if (mesg.getAvgTemperature() != null) {
      temperature = Integer.valueOf(mesg.getAvgTemperature().toString());
    }
  }

  double getDistance() {
    return distance;
  }

  double getAverageSpeed() {
    return averageSpeed;
  }

  Duration getElapsedTime() {
    return elapsedTime;
  }

  Duration getTimeWithoutBreaks() {
    return timeWithoutBreaks;
  }

  Integer getTotalAscent() {
    return totalAscent;
  }

  Integer getTemperature() {
    return temperature;
  }
}
