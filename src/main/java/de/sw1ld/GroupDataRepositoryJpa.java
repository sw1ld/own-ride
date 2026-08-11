package de.sw1ld;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GroupDataRepositoryJpa implements GroupDataRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public Optional<GroupData> findById(UUID id) {
    try {
      return Optional.of(
          entityManager
              .createNamedQuery(GroupData.QUERY_FIND_BY_ID, GroupData.class)
              .setParameter("id", id)
              .getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public void persist(GroupData group) {
    entityManager.persist(group);
  }

  @Override
  public List<GroupData> findInRange(LocalDate start, LocalDate end) {

    return entityManager
        .createNamedQuery(GroupData.QUERY_FIND_IN_RANGE, GroupData.class)
        .setParameter("start", start)
        .setParameter("end", end)
        .getResultList();
  }

  @Override
  public void delete(UUID id) {
    findById(id).ifPresent(entityManager::remove);
  }
}
