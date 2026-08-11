package de.sw1ld;

import jakarta.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

record Cursor(LocalDate date, UUID id) {

  private static final String SEPARATOR = " ";

  String encode() {
    String raw = date + SEPARATOR + id;
    return Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  static @Nullable Cursor decode(@Nullable String encoded) {
    if (encoded == null) return null; // null for first page
    String raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
    String[] parts = raw.split(SEPARATOR, 2);
    return new Cursor(LocalDate.parse(parts[0]), UUID.fromString(parts[1]));
  }
}
