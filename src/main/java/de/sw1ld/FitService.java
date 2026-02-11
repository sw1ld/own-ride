package de.sw1ld;

import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@ApplicationScoped
public class FitService {

  private final ActivityDataRepository activityDataRepository;

  public FitService(ActivityDataRepository activityDataRepository) {
    this.activityDataRepository = activityDataRepository;
  }

  FitData fetchDetailsBy(UUID id) {
    ActivityData data =
        activityDataRepository
            .findById(id)
            .orElseThrow(() -> new IllegalStateException("Resource not found for Id: " + id));

    return new FitData(data);
  }

  List<FitData> fetchDetails(@Nullable Integer year) {
    if (year == null) {
      return activityDataRepository.findAll().stream().map(FitData::new).toList();
    } else {
      return activityDataRepository.findByYear(year).stream().map(FitData::new).toList();
    }
  }

  List<Integer> getAvailableYears() {
    int currentYear = LocalDate.now().getYear();
    int minYear = activityDataRepository.findMinYear().orElse(currentYear);

    // Wir wollen eine absteigende Liste (aktuellstes Jahr zuerst)
    return IntStream.rangeClosed(minYear, currentYear).boxed().sorted((a, b) -> b - a).toList();
  }
}
