package de.sw1ld;

public record Position(float lat, float lon, float altitude) {

  public static Position fromSemicircles(float lat, float lon, float altitude) {
    return new Position((float) toDegree(lat), (float) toDegree(lon), altitude);
  }

  private static double toDegree(double semicircle) {
    return semicircle * (180.0 / Math.pow(2, 31));
  }
}
