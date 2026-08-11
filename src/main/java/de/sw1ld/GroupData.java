package de.sw1ld;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "activity_group")
@NamedQueries({
  @NamedQuery(
      name = GroupData.QUERY_FIND_BY_ID,
      query = "SELECT g FROM GroupData g WHERE g.id = :id"),
  @NamedQuery(
      name = GroupData.QUERY_FIND_IN_RANGE,
      query =
          "SELECT DISTINCT g FROM GroupData g JOIN g.activities a WHERE a.date >="
              + " :start AND a.date <= :end") // TODO in between statement?
})
public class GroupData {
  public static final String QUERY_FIND_BY_ID = "GroupData.findById";
  public static final String QUERY_FIND_IN_RANGE = "GroupData.findInRange";

  @Id private UUID id;
  private String name;
  private Integer rate;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private List<ActivityData> activities = new ArrayList<>();

  @Column(columnDefinition = "TEXT")
  private String thumbnail;

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

  public Integer getRate() {
    return rate;
  }

  public void setRate(Integer rate) {
    this.rate = rate;
  }

  public List<ActivityData> getActivities() {
    return activities;
  }

  public void setActivities(List<ActivityData> activities) {
    this.activities = activities;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }
}
