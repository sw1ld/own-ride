package de.sw1ld;

import java.util.UUID;

public record Bike(UUID id, String producer, String name) {
  public Bike(BikeData data) {
    this(data.getId(), data.getProducer(), data.getName());
  }
}
