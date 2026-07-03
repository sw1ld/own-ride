package de.sw1ld;

import java.util.UUID;

public record UploadSuccess(String fileName, UUID id) implements UploadResult {

  @Override
  public String fileName() {
    return fileName;
  }

  @Override
  public Result result() {
    return Result.SUCCESS;
  }
}
