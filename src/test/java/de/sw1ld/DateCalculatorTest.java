package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

class DateCalculatorTest {

  public static final LocalDate NOW = LocalDate.now();

  @Test
  void nullableDefaults() {
    DateCalculator cut = new DateCalculator(null, null, null);

    LocalDate firstDayOfCurrentYear = LocalDate.of(NOW.getYear(), Month.JANUARY, 1);
    LocalDate lastDayOfCurrentYear = LocalDate.of(NOW.getYear(), Month.DECEMBER, 31);
    assertThat(cut.start()).isEqualTo(firstDayOfCurrentYear);
    assertThat(cut.end()).isEqualTo(lastDayOfCurrentYear);
    assertThat(cut.hasNext()).isFalse();
    assertThat(cut.hasPrevious()).isFalse();
  }

  @Test
  void currentYear() {
    DateCalculator cut = new DateCalculator("YEAR", null, null);

    LocalDate firstDayOfCurrentYear = LocalDate.of(NOW.getYear(), Month.JANUARY, 1);
    LocalDate lastDayOfCurrentYear = LocalDate.of(NOW.getYear(), Month.DECEMBER, 31);
    assertThat(cut.start()).isEqualTo(firstDayOfCurrentYear);
    assertThat(cut.end()).isEqualTo(lastDayOfCurrentYear);
    assertThat(cut.hasNext()).isFalse();
  }

  @Test
  void lastYear() {
    DateCalculator cut = new DateCalculator("YEAR", 1, null);

    LocalDate firstDayOfLastYear = LocalDate.of(NOW.getYear() - 1, Month.JANUARY, 1);
    LocalDate lastDayOfLastYear = LocalDate.of(NOW.getYear() - 1, Month.DECEMBER, 31);
    assertThat(cut.start()).isEqualTo(firstDayOfLastYear);
    assertThat(cut.end()).isEqualTo(lastDayOfLastYear);
    assertThat(cut.hasNext()).isTrue();
  }

  @Test
  void lastMonth() {
    DateCalculator cut = new DateCalculator("MONTH", 1, null);

    LocalDate firstDayOfLastMonth = LocalDate.of(NOW.getYear(), NOW.getMonth().minus(1), 1);
    LocalDate lastDayOfLastMonth = LocalDate.of(NOW.getYear(), NOW.getMonth(), 1).minusDays(1);
    assertThat(cut.start()).isEqualTo(firstDayOfLastMonth);
    assertThat(cut.end()).isEqualTo(lastDayOfLastMonth);
    assertThat(cut.hasNext()).isTrue();
  }

  @Test
  void currentWeek() {
    DateCalculator cut = new DateCalculator("WEEK", null, null);

    LocalDate mondayOfCurrentWeek = NOW.with(DayOfWeek.MONDAY);
    LocalDate sundayOfCurrentWeek = mondayOfCurrentWeek.plusDays(6);
    assertThat(cut.start()).isEqualTo(mondayOfCurrentWeek);
    assertThat(cut.end()).isEqualTo(sundayOfCurrentWeek);
    assertThat(cut.hasNext()).isFalse();
  }

  @Test
  void lastWeek() {
    DateCalculator cut = new DateCalculator("WEEK", 1, null);

    LocalDate mondayOfLastWeek = NOW.with(DayOfWeek.MONDAY).minusWeeks(1);
    LocalDate sundayOfLastWeek = mondayOfLastWeek.plusDays(6);
    assertThat(cut.start()).isEqualTo(mondayOfLastWeek);
    assertThat(cut.end()).isEqualTo(sundayOfLastWeek);
    assertThat(cut.hasNext()).isTrue();
  }

  @Test
  void earliestActivityDate() {
    LocalDate earliestActivityDate = LocalDate.of(NOW.getYear() - 1, Month.MARCH, 15);
    DateCalculator cutCurrentYear = new DateCalculator("YEAR", null, earliestActivityDate);

    assertThat(cutCurrentYear.hasNext()).isFalse();
    assertThat(cutCurrentYear.hasPrevious()).isTrue();

    DateCalculator cutLastYear = new DateCalculator("YEAR", 1, earliestActivityDate);
    assertThat(cutLastYear.hasNext()).isTrue();
    assertThat(cutLastYear.hasPrevious()).isFalse();
  }

  @Test
  void all_usesEarliestActivityDateAsStart() {
    LocalDate earliestActivityDate = LocalDate.of(2022, Month.MARCH, 15);
    DateCalculator cut = new DateCalculator("ALL", null, earliestActivityDate);

    assertThat(cut.start()).isEqualTo(earliestActivityDate);
    assertThat(cut.end()).isEqualTo(NOW);
  }
}
