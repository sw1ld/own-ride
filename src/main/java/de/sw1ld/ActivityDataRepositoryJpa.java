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
public class ActivityDataRepositoryJpa implements ActivityDataRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public Optional<ActivityData> findById(UUID id) {

    try {
      return Optional.of(
          entityManager
              .createNamedQuery(ActivityData.QUERY_FIND_BY_ID, ActivityData.class)
              .setParameter("id", id)
              .getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<ActivityData> findByYear(int year) {
    LocalDate start = LocalDate.of(year, 1, 1);
    LocalDate end = start.plusYears(1);

    return entityManager
        .createNamedQuery(ActivityData.QUERY_FIND_BY_YEAR, ActivityData.class)
        .setParameter("startOfYear", start)
        .setParameter("startOfNextYear", end)
        .getResultList();
  }

  @Override
  public List<ActivityData> findAll() {
    return entityManager
        .createNamedQuery(ActivityData.QUERY_FIND_ALL, ActivityData.class)
        .getResultList();
  }

  @Override
  public Optional<Integer> findMinYear() {
    LocalDate minDate =
        entityManager
            .createNamedQuery(ActivityData.QUERY_FIND_MIN_DATE, LocalDate.class)
            .getSingleResult();
    return Optional.ofNullable(minDate).map(LocalDate::getYear);
  }
}
