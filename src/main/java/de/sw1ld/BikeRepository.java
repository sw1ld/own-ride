package de.sw1ld;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BikeRepository {
  Optional<BikeData> findById(UUID id);

  List<BikeData> findAll();

  List<BikeDistance> fetchTotalDistances();

  void persist(BikeData bike);

  BikeData merge(BikeData bike);

  boolean delete(UUID id);
}
