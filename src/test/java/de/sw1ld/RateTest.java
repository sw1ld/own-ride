package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RateTest {

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5})
  void instantiateRateWorks(Integer validInput) {
    Rate cut = new Rate(validInput);

    assertThat(cut.value()).isEqualTo(validInput);
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 6, 100})
  void instantiateRateFailsForInvalidRange(Integer invalidInput) {
    assertThatThrownBy(() -> new Rate(invalidInput))
        .hasMessage("Rate value must be between 0 and 5");
  }
}
