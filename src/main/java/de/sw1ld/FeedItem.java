package de.sw1ld;

import java.util.List;
import java.util.UUID;

public interface FeedItem {
  UUID id();

  FeedType type();

  String displayName();

  String date();

  String distance();

  String duration();

  String elapsedTime();

  String avgSpeed();

  Integer rate();

  String temperature();

  String totalAscent();

  Bike bike();

  String thumbnail();

  default List<Position> positions() {
    return List.of();
  }
}
