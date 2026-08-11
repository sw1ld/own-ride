package de.sw1ld.thumbnails;

import de.sw1ld.Position;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ThumbnailService {

  private static final int THUMBNAIL_WIDTH = 128;
  private static final int THUMBNAIL_HEIGHT = 128;

  private final Template thumbnail;

  ThumbnailService(@Location("thumbnail.svg") Template thumbnail) {
    this.thumbnail = thumbnail;
  }

  public String renderSvg(List<Position> positions) {
    if (positions == null || positions.size() < 2) {
      return renderPlaceholder();
    }

    List<double[]> projected = positions.stream().map(ThumbnailService::project).toList();

    List<double[]> simplified = RamerDouglasPeucker.simplify(projected, 0.0005);
    List<ScreenPoint> screen =
        ViewportScaler.fit(simplified, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, 10);

    String pointsAttr =
        screen.stream()
            .map(sp -> "%.1f,%.1f".formatted(sp.x(), sp.y()))
            .collect(Collectors.joining(" "));

    ScreenPoint start = screen.getFirst();
    ScreenPoint end = screen.getLast();

    return thumbnail
        .data("width", THUMBNAIL_WIDTH)
        .data("height", THUMBNAIL_HEIGHT)
        .data("strokeWidth", 3)
        .data("points", pointsAttr)
        .data("startX", start.x())
        .data("startY", start.y())
        .data("endX", end.x())
        .data("endY", end.y())
        .render();
  }

  private static double[] project(Position p) {
    return new double[] {
      p.lon(), Math.toDegrees(Math.log(Math.tan(Math.PI / 4 + Math.toRadians(p.lat()) / 2)))
    };
  }

  private String renderPlaceholder() {
    return "<svg xmlns='http://www.w3.org/2000/svg' width='%d' height='%d'></svg>"
        .formatted(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
  }
}
