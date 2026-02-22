package de.sw1ld;

import java.util.UUID;

public record UploadResult(String fileName, boolean success, String message, UUID id) {
  public static UploadResult error(String fileName, String message) {
    return new UploadResult(fileName, false, message, null);
  }

  public static UploadResult success(String fileName, UUID id) {
    return new UploadResult(fileName, true, "Successfully uploaded", id);
  }
}
