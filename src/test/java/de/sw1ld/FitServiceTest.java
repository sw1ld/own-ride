package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FitServiceTest {

  private static final String TEST_FIT_FILE = "2021-04-27_Route1.fit";
  private static final String UNKNOWN_FIT_FILE = "unknown.fit";

  FitService cut;

  @BeforeEach
  void setUp() {
    cut = new FitService("testdata");
  }

  @Test
  void fetchDataBy() {
    FitData result = cut.fetchDataBy(TEST_FIT_FILE);

    assertThat(result.name()).isEqualTo(TEST_FIT_FILE);
    assertThat(result.date()).isEqualTo(LocalDate.of(2021, 4, 27));
    assertThat(result.distance()).isCloseTo(34.68, within(0.005));
    assertThat(result.avgSpeed()).isCloseTo(21.93, within(0.005));
    assertThat(result.positions()).isNotEmpty();
  }

  @Test
  void fetchDataByUnknown() {
    FitData result = cut.fetchDataBy(UNKNOWN_FIT_FILE);

    assertThat(result).isNull();
  }

  @Test
  void fetchData() {
    List<FitData> result = cut.fetchData();

    assertThat(result).hasSize(2);
  }
}
