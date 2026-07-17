package de.sw1ld;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "bike")
@NamedQueries({
  @NamedQuery(name = BikeData.QUERY_FIND_ALL, query = "SELECT b FROM BikeData b"),
  @NamedQuery(
      name = BikeData.QUERY_FIND_BY_ID,
      query = "SELECT b FROM BikeData b WHERE b.id = :id"),
  @NamedQuery(
      name = BikeData.QUERY_TOTAL_DISTANCES,
      query =
          "SELECT new de.sw1ld.BikeDistance(a.bike.id, SUM(a.distance)) "
              + "FROM ActivityData a WHERE a.bike IS NOT NULL GROUP BY a.bike.id")
})
public class BikeData {

  public static final String QUERY_FIND_ALL = "BikeData.findAll";
  public static final String QUERY_FIND_BY_ID = "BikeData.findById";
  public static final String QUERY_TOTAL_DISTANCES = "BikeData.fetchTotalDistances";

  @Id private UUID id;
  private String producer;
  private String name;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getProducer() {
    return producer;
  }

  public void setProducer(String producer) {
    this.producer = producer;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
