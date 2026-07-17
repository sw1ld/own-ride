package de.sw1ld;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BikeRepositoryJpa implements BikeRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public Optional<BikeData> findById(UUID id) {
    try {
      return Optional.of(
          entityManager
              .createNamedQuery(BikeData.QUERY_FIND_BY_ID, BikeData.class)
              .setParameter("id", id)
              .getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<BikeData> findAll() {
    return entityManager.createNamedQuery(BikeData.QUERY_FIND_ALL, BikeData.class).getResultList();
  }

  @Override
  public List<BikeDistance> fetchTotalDistances() {
    return entityManager
        .createNamedQuery(BikeData.QUERY_TOTAL_DISTANCES, BikeDistance.class)
        .getResultList();
  }

  @Override
  public void persist(BikeData bike) {
    entityManager.persist(bike);
  }

  @Override
  public BikeData merge(BikeData bike) {
    return entityManager.merge(bike);
  }

  @Override
  public boolean delete(UUID id) {
    Optional<BikeData> bikeToDelete = findById(id);
    if (bikeToDelete.isPresent()) {
      entityManager.remove(bikeToDelete.get());
      return true;
    }
    return false;
  }
}
