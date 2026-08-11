package de.sw1ld;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupDataRepository {
  Optional<GroupData> findById(UUID id);

  List<GroupData> findInRange(LocalDate from, LocalDate to);

  void persist(GroupData group);

  void delete(UUID id);
}
