package de.sw1ld;

import com.garmin.fit.Decode;
import com.garmin.fit.MesgBroadcaster;
import io.quarkus.runtime.util.ClassPathUtils;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class FitService {

  private final String sourcePath;

  public FitService(@ConfigProperty(name = "source-path") String sourcePath) {
    this.sourcePath = sourcePath;
  }

  FitData fetchDataBy(String fileName) {
    Decode decode = new Decode();
    MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
    StatsListener stats = new StatsListener();
    broadcaster.addListener(stats);
    String fullPath = sourcePath + "/" + fileName;

    try (InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(fullPath)) {

      if (is == null) {
        return null;
      }

      decode.read(is, broadcaster);

      return new FitData(
          fileName,
          stats.getDate(),
          stats.getTotalDistance(),
          stats.getAverageSpeed(),
          stats.getPositions());

    } catch (IOException e) {
      throw new IllegalStateException("Resource not found: " + fullPath);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  List<FitData> fetchData() {
    List<FitData> fitResponses = new ArrayList<>();

    try {
      ClassPathUtils.consumeAsPaths(
          sourcePath,
          root -> {
            try {
              Files.walk(root)
                  .filter(Files::isRegularFile)
                  .forEach(
                      file -> {
                        String filename = file.getFileName().toString();
                        fitResponses.add(fetchDataBy(filename));
                      });
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    } catch (IOException e) {
      throw new IllegalStateException("SourcePath does not exist: " + sourcePath, e);
    }

    return fitResponses;
  }
}
