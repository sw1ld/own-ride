package de.sw1ld;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityDataRepository {
  Optional<ActivityData> findById(UUID id);

  List<ActivityData> findByYear(int year);

  List<ActivityData> findAll();

  Optional<Integer> findMinYear();

  Optional<ActivityData> findByTimeCreated(LocalDateTime timeCreated);

  boolean delete(UUID id);
}
