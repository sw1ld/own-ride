package de.sw1ld;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ActivityRawRepositoryJpa implements ActivityRawRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public Optional<ActivityRaw> findById(UUID id) {

    try {
      return Optional.of(
          entityManager
              .createNamedQuery(ActivityRaw.QUERY_FIND_BY_ID, ActivityRaw.class)
              .setParameter("id", id)
              .getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}
