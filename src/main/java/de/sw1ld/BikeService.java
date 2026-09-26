package de.sw1ld;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class BikeService {

  private final BikeRepository bikeRepository;
  private final ActivityDataRepository activityDataRepository;

  public BikeService(BikeRepository bikeRepository, ActivityDataRepository activityDataRepository) {
    this.bikeRepository = bikeRepository;
    this.activityDataRepository = activityDataRepository;
  }

  public List<BikeResponse> fetchBikes() {
    Map<UUID, Double> distanceMap =
        bikeRepository.fetchTotalDistances().stream()
            .collect(Collectors.toMap(BikeDistance::bikeId, BikeDistance::totalDistance));

    return bikeRepository.findAll().stream()
        .map(
            bikeData -> {
              double totalDistance = distanceMap.getOrDefault(bikeData.getId(), 0.0);
              return new BikeResponse(
                  new Bike(bikeData), Prettyfier.distanceWithUnit(totalDistance));
            })
        .toList();
  }

  @Transactional
  public BikeData addBike(String producer, String name) {
    validBikeInputOrThrow(producer, name);

    BikeData bike = new BikeData();
    bike.setId(UUID.randomUUID());
    bike.setProducer(producer.trim());
    bike.setName(name.trim());
    bikeRepository.persist(bike);
    return bike;
  }

  @Transactional
  public Optional<BikeData> updateBike(UUID bikeId, String producer, String name) {
    validBikeInputOrThrow(producer, name);

    Optional<BikeData> bikeInInventory = findBike(bikeId);
    if (bikeInInventory.isEmpty()) {
      return Optional.empty();
    }
    BikeData bike = bikeInInventory.get();
    bike.setProducer(producer.trim());
    bike.setName(name.trim());
    return Optional.of(bikeRepository.merge(bike));
  }

  @Transactional
  public void deleteBike(UUID bikeId) {

    activityDataRepository.removeBikeAssignments(bikeId);
    bikeRepository.delete(bikeId);
  }

  public Optional<BikeData> findBike(UUID bikeId) {
    return bikeRepository.findById(bikeId);
  }

  public List<Bike> findAll() {
    return bikeRepository.findAll().stream().map(Bike::new).toList();
  }

  private static void validBikeInputOrThrow(String producer, String name) {
    if (producer == null || producer.isBlank() || name == null || name.isBlank()) {
      throw new IllegalArgumentException("Producer and bike name must not be empty");
    }
  }
}
