package de.sw1ld;

import com.garmin.fit.DateTime;
import java.time.LocalDate;
import java.time.ZoneId;

public record Position(double lat, double lon, float altitude, LocalDate timestamp) {

  public Position(double latSemicircle, double lonSemicircle, Float altitude, DateTime timestamp) {
    this(
        toDegree(latSemicircle),
        toDegree(lonSemicircle),
        altitude != null ? altitude : 0.0f,
        convertDate(timestamp));
  }

  private static double toDegree(double semicircle) {
    // how to verify it was not already converted?
    return semicircle * (180.0 / Math.pow(2, 31));
  }

  private static LocalDate convertDate(DateTime dt) {
    return dt.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
