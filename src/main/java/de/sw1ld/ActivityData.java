package de.sw1ld;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "activity_data")
@NamedQuery(
    name = ActivityData.QUERY_FIND_BY_ID,
    query = "SELECT ed FROM ActivityData ed WHERE id = :id")
@NamedQuery(
    name = ActivityData.QUERY_FIND_BY_YEAR,
    query =
        "SELECT ed FROM ActivityData ed WHERE ed.date >= :startOfYear AND ed.date <"
            + " :startOfNextYear")
@NamedQuery(name = ActivityData.QUERY_FIND_ALL, query = "SELECT ed FROM ActivityData ed")
@NamedQuery(
    name = ActivityData.QUERY_FIND_MIN_DATE,
    query = "SELECT MIN(ed.date) FROM ActivityData ed")
@NamedQuery(
    name = ActivityData.QUERY_FIND_BY_TIME_CREATED,
    query = "SELECT ed FROM ActivityData ed WHERE ed.timeCreated = :timeCreated")
public class ActivityData {

  public static final String QUERY_FIND_BY_ID = "ExtractedData.findById";
  public static final String QUERY_FIND_BY_YEAR = "ExtractedData.findByYear";
  public static final String QUERY_FIND_ALL = "ExtractedData.findAll";
  public static final String QUERY_FIND_MIN_DATE = "ExtractedData.findMinDate";
  public static final String QUERY_FIND_BY_TIME_CREATED = "ExtractedData.findByTimeCreated";

  @Id @Column private UUID id;

  @OneToOne
  @JoinColumn(name = "activity_raw_id")
  private ActivityRaw activityRaw;

  @Column(length = 30, nullable = false)
  private String name;

  @Column(nullable = false)
  private LocalDate date;

  @Column private Double distance;

  @Column private Duration duration;

  @Column(name = "avg_speed")
  private Double avgSpeed;

  @Column(name = "max_speed")
  private Double maxSpeed;

  @Column(name = "total_ascent")
  private Integer totalAscent;

  @Column(name = "time_created")
  private LocalDateTime timeCreated;

  @Column private Integer temperature;

  @Column
  @JdbcTypeCode(SqlTypes.JSON)
  @Convert(converter = PositionListJsonConverter.class)
  private List<Position> positions;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ActivityRaw getActivity() {
    return activityRaw;
  }

  public void setActivity(ActivityRaw activityRaw) {
    this.activityRaw = activityRaw;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public Double getAvgSpeed() {
    return avgSpeed;
  }

  public void setAvgSpeed(Double avgSpeed) {
    this.avgSpeed = avgSpeed;
  }

  public Double getMaxSpeed() {
    return maxSpeed;
  }

  public void setMaxSpeed(Double maxSpeed) {
    this.maxSpeed = maxSpeed;
  }

  public Integer getTotalAscent() {
    return totalAscent;
  }

  public void setTotalAscent(Integer totalAscent) {
    this.totalAscent = totalAscent;
  }

  public LocalDateTime getTimeCreated() {
    return timeCreated;
  }

  public void setTimeCreated(LocalDateTime timeCreated) {
    this.timeCreated = timeCreated;
  }

  public Integer getTemperature() {
    return temperature;
  }

  public void setTemperature(Integer temperature) {
    this.temperature = temperature;
  }

  public List<Position> getPositions() {
    return positions;
  }

  public void setPositions(List<Position> positions) {
    this.positions = positions;
  }
}
