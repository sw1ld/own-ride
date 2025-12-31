package de.sw1ld;

import com.garmin.fit.DateTime;
import java.time.LocalDate;
import java.time.ZoneId;

public record Position(double lat, double lon, LocalDate timestamp) {

  public Position(double latSemicircle, double lonSemicircle, DateTime timestamp) {
    this(toDegree(latSemicircle), toDegree(lonSemicircle), convertDate(timestamp));
  }

  private static double toDegree(double semicircle) {
    // how to verify it was not already converted?
    return semicircle * (180.0 / Math.pow(2, 31));
  }

  private static LocalDate convertDate(DateTime dt) {
    return dt.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
