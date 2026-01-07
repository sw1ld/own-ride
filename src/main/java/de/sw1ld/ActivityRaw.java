package de.sw1ld;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "activity_raw")
@NamedQuery(
    name = ActivityRaw.QUERY_FIND_BY_ID,
    query = "SELECT a FROM ActivityRaw a WHERE id = :id")
public class ActivityRaw {

  public static final String QUERY_FIND_BY_ID = "ActivityRaw.findById";

  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(length = 30, nullable = false)
  private String name;

  @Column(name = "fit_file", nullable = false)
  private byte[] fitFile;

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false)
  private LocalDateTime uploadedAt;

  @Column(name = "last_modified", nullable = false)
  private LocalDateTime lastModified;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public byte[] getFitFile() {
    return fitFile;
  }

  public void setFitFile(byte[] fitFile) {
    this.fitFile = fitFile;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public LocalDateTime getLastModified() {
    return lastModified;
  }

  public void setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
  }
}
