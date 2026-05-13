package de.sw1ld;

public record Rate(Integer value) {

  public Rate {
    if (value < 0 || value > 5) {
      throw new IllegalArgumentException("Rate value must be between 0 and 5");
    }
  }
}
