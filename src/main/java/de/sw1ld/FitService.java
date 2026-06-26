package de.sw1ld;

import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@ApplicationScoped
public class FitService {

  private final ActivityDataRepository activityDataRepository;

  public FitService(ActivityDataRepository activityDataRepository) {
    this.activityDataRepository = activityDataRepository;
  }

  Optional<FitData> fetchActivityBy(UUID id) {
    return activityDataRepository.findById(id).map(FitData::new);
  }

  List<FitData> fetchActivities(@Nullable Integer year) {
    if (year == null) {
      return activityDataRepository.findAll().stream().map(FitData::new).toList();
    } else {
      return activityDataRepository.findByYear(year).stream().map(FitData::new).toList();
    }
  }

  List<Integer> getAvailableYears() {
    int currentYear = LocalDate.now().getYear();
    int minYear = activityDataRepository.findMinYear().orElse(currentYear);

    // We want a descending list (most recent year first)
    return IntStream.rangeClosed(minYear, currentYear).boxed().sorted((a, b) -> b - a).toList();
  }

  @Transactional
  boolean deleteActivity(UUID id) {
    return activityDataRepository.delete(id);
  }
}
