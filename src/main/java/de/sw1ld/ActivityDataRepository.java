package de.sw1ld;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityDataRepository {
  Optional<ActivityData> findById(UUID id);

  List<ActivityData> findByYear(int year);

  List<ActivityData> findAll();

  Optional<Integer> findMinYear();
}
