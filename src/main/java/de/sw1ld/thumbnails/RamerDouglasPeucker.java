package de.sw1ld.thumbnails;

import java.util.ArrayList;
import java.util.List;

final class RamerDouglasPeucker {
  private RamerDouglasPeucker() {}

  static List<double[]> simplify(List<double[]> points, double epsilon) {
    if (points.size() < 3) return points;

    double maxDist = 0;
    int index = 0;
    double[] first = points.getFirst();
    double[] last = points.getLast();

    for (int i = 1; i < points.size() - 1; i++) {
      double d = perpendicularDistance(points.get(i), first, last);
      if (d > maxDist) {
        maxDist = d;
        index = i;
      }
    }

    if (maxDist > epsilon) {
      List<double[]> left = simplify(points.subList(0, index + 1), epsilon);
      List<double[]> right = simplify(points.subList(index, points.size()), epsilon);
      List<double[]> result = new ArrayList<>(left.subList(0, left.size() - 1));
      result.addAll(right);
      return result;
    }
    return List.of(first, last);
  }

  private static double perpendicularDistance(double[] p, double[] a, double[] b) {
    double dx = b[0] - a[0], dy = b[1] - a[1];
    double norm = Math.sqrt(dx * dx + dy * dy);
    if (norm == 0) return Math.hypot(p[0] - a[0], p[1] - a[1]);
    return Math.abs(dy * p[0] - dx * p[1] + b[0] * a[1] - b[1] * a[0]) / norm;
  }
}
