package de.sw1ld;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BikeResourceTest {

  private static final String BIKES_PATH = "/bikes";
  private static final String BIKE_ID_PATH = BIKES_PATH + "/id/%s";

  private BikeService bikeService;

  @BeforeEach
  void setup() {
    bikeService = mock(BikeService.class);
    QuarkusMock.installMockForType(bikeService, BikeService.class);

    when(bikeService.fetchBikes())
        .thenReturn(
            List.of(new BikeResponse(new Bike(UUID.randomUUID(), "Canyon", "Endurace"), "100 km")));
  }

  @Test
  void fetchAllBikesAsJson() {
    List<BikeResponse> response =
        RestAssured.given()
            .when()
            .accept(ContentType.JSON)
            .get(BIKES_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .body()
            .as(new TypeRef<>() {});

    assertThat(response)
        .hasSize(1)
        .element(0)
        .satisfies(
            element -> {
              assertThat(element.bike().producer()).isEqualTo("Canyon");
              assertThat(element.bike().name()).isEqualTo("Endurace");
              assertThat(element.totalDistance()).isEqualTo("100 km");
            });
  }

  @Test
  void fetchAllBikesAsHtml() {
    RestAssured.given()
        .when()
        .accept(ContentType.HTML)
        .get(BIKES_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(
            containsString("Add New Bike"),
            containsString("Inventory"),
            containsString("Endurace"),
            containsString("Total Distance"));
  }

  @Test
  void fetchBikeAsJson() {
    BikeData bikeData = mock(BikeData.class);
    when(bikeData.getId()).thenReturn(UUID.randomUUID());
    when(bikeData.getProducer()).thenReturn("Canyon");
    when(bikeData.getName()).thenReturn("Endurace");

    when(bikeService.findBike(any())).thenReturn(Optional.of(bikeData));

    Bike response =
        RestAssured.given()
            .when()
            .accept(MediaType.APPLICATION_JSON)
            .get(BIKE_ID_PATH.formatted(bikeData.getId()))
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .body()
            .as(Bike.class);

    assertThat(response.producer()).isEqualTo("Canyon");
    assertThat(response.name()).isEqualTo("Endurace");
  }

  @Test
  void bikeNotFoundAsJson() {
    when(bikeService.findBike(any())).thenReturn(Optional.empty());

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get(BIKE_ID_PATH.formatted(UUID.randomUUID()))
        .then()
        .statusCode(404)
        .body(containsString("Not Found"), containsString("does not exist"));
  }
}
