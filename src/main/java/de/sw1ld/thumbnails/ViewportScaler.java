package de.sw1ld.thumbnails;

import java.util.ArrayList;
import java.util.List;

final class ViewportScaler {
  private ViewportScaler() {}

  static List<ScreenPoint> fit(List<double[]> points, int width, int height, int padding) {
    double minX = points.stream().mapToDouble(p -> p[0]).min().orElse(0);
    double maxX = points.stream().mapToDouble(p -> p[0]).max().orElse(0);
    double minY = points.stream().mapToDouble(p -> p[1]).min().orElse(0);
    double maxY = points.stream().mapToDouble(p -> p[1]).max().orElse(0);

    double spanX = Math.max(maxX - minX, 1e-9);
    double spanY = Math.max(maxY - minY, 1e-9);

    double usableW = width - 2.0 * padding;
    double usableH = height - 2.0 * padding;
    double scale = Math.min(usableW / spanX, usableH / spanY);

    // center the drawing
    double drawnW = spanX * scale;
    double drawnH = spanY * scale;
    double offsetX = padding + (usableW - drawnW) / 2.0;
    double offsetY = padding + (usableH - drawnH) / 2.0;

    List<ScreenPoint> result = new ArrayList<>(points.size());
    for (double[] p : points) {
      double sx = offsetX + (p[0] - minX) * scale;
      // flip Y: mercator Y grows north, SVG Y grows downward
      double sy = offsetY + (maxY - p[1]) * scale;
      result.add(new ScreenPoint(sx, sy));
    }
    return result;
  }
}
