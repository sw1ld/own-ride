package de.sw1ld;

public interface UploadResult {
  String fileName();

  Result result();

  default UploadSuccess asUploadSuccess() {
    return (UploadSuccess) this;
  }

  default UploadFailure asUploadFailure() {
    return (UploadFailure) this;
  }
}
