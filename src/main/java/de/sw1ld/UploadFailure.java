package de.sw1ld;

import org.jspecify.annotations.NonNull;

public record UploadFailure(@NonNull String fileName, @NonNull String message)
    implements UploadResult {

  @Override
  public String fileName() {
    return fileName;
  }

  @Override
  public Result result() {
    return Result.FAILURE;
  }
}
