package de.sw1ld;

import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ActivityDataRepository {
  Optional<ActivityData> findById(UUID id);

  List<ActivityData> findByIds(Set<UUID> ids);

  List<PerformanceData> fetchPerformanceData(LocalDate start, LocalDate end);

  List<ActivityData> fetchFeed(@Nullable Cursor cursor, int limit);

  Optional<LocalDate> findEarliestActivityDate();

  Optional<ActivityData> findByTimeCreated(LocalDateTime timeCreated);

  boolean delete(UUID id);

  void removeBikeAssignments(UUID bikeId);
}
