package de.sw1ld.thumbnails;

import static org.assertj.core.api.Assertions.assertThat;

import de.sw1ld.Position;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ThumbnailServiceTest {

  @Inject ThumbnailService cut;

  @Test
  void simpleDiagonalPath() {
    List<Position> positions =
        List.of(
            new Position(0.0, 0.0, 0, LocalDate.now()), new Position(1.0, 1.0, 0, LocalDate.now()));

    String result = cut.renderSvg(positions);

    assertThat(result)
        .contains(
            "svg", // metadata
            "128", // width/height of svg
            "polyline points", // line gets drawn
            "circle", // round marker for start/end point
            "10.0,118.0", // first point (10 padding)
            "10.0,118.0" // second point
            );
  }
}
