package de.sw1ld;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record DateCalculator(FilterUnit filterUnit, int offset, LocalDate earliestActivityDate) {

  private static final LocalDate NOW = LocalDate.now();

  public DateCalculator(
      @Nullable String filterUnit,
      @Nullable Integer offset,
      @Nullable LocalDate earliestActivityDate) {
    this(
        parseFilterUnit(filterUnit),
        offset == null ? 0 : offset,
        earliestActivityDate == null ? NOW : earliestActivityDate);
  }

  LocalDate start() {
    return switch (filterUnit) {
      case ALL -> earliestActivityDate;
      case YEAR -> LocalDate.of(NOW.getYear(), 1, 1).minusYears(offset);
      case MONTH -> LocalDate.of(NOW.getYear(), NOW.getMonth(), 1).minusMonths(offset);
      case WEEK -> NOW.with(DayOfWeek.MONDAY).minusWeeks(offset);
    };
  }

  LocalDate end() {
    return switch (filterUnit) {
      case ALL -> NOW;
      case YEAR -> start().plusYears(1).minusDays(1);
      case MONTH -> start().plusMonths(1).minusDays(1);
      case WEEK -> start().plusDays(6);
    };
  }

  boolean hasNext() {
    return filterUnit != FilterUnit.ALL && offset > 0;
  }

  boolean hasPrevious() {
    return filterUnit != FilterUnit.ALL && start().isAfter(earliestActivityDate);
  }

  private static FilterUnit parseFilterUnit(String filterUnit) {
    if (filterUnit != null) {
      try {
        return FilterUnit.valueOf(filterUnit);
      } catch (IllegalArgumentException e) {
        return FilterUnit.YEAR;
      }
    }
    return FilterUnit.YEAR;
  }
}
