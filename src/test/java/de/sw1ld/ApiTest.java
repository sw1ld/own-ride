package de.sw1ld;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class ApiTest {

  private static final int YEAR = 2021;

  @Test
  @Order(1)
  void initialApplication_hasNoContent() {
    given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", YEAR)
        .when()
        .get("/stats")
        .then()
        .statusCode(200)
        .body("rides", equalTo(0))
        .body("distance", equalTo("0.00 km"))
        .body("tourDates.2021-01-01", equalTo(0.0F))
        .body("tourDates.2021-04-27", equalTo(0.0F));

    given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", YEAR)
        .when()
        .get("/activities")
        .then()
        .statusCode(200)
        .body("", empty());

    UUID randomId = UUID.randomUUID();
    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/activities/id/" + randomId)
        .then()
        .statusCode(404)
        .body("detail", equalTo("Activity with id '%s' does not exist".formatted(randomId)));

    BikeResponse[] bikes =
        given()
            .accept(MediaType.APPLICATION_JSON)
            .when()
            .get("/bikes")
            .then()
            .statusCode(200)
            .extract()
            .as(BikeResponse[].class);

    assertThat(bikes).isEmpty();
  }

  @Test
  @Order(2)
  void uploadFile() {
    File fitFile = new File("src/test/resources/testdata/2021-04-27_Route1.fit");

    given()
        .contentType(ContentType.MULTIPART)
        .accept(MediaType.APPLICATION_JSON)
        .multiPart("file", fitFile)
        .multiPart("file", "") // empty input by HTML form
        .when()
        .post("/upload")
        .then()
        .statusCode(201)
        .body("", hasSize(1));

    given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", YEAR)
        .when()
        .get("/stats")
        .then()
        .statusCode(200)
        .body("rides", equalTo(1))
        .body("distance", equalTo("34.68 km"))
        .body("tourDates.2021-01-01", equalTo(0.0F))
        .body("tourDates.2021-04-27", greaterThan(34.0F));

    ActivityResponse activity = firstActivity(YEAR);
    assertThat(activity.displayName()).isEqualTo("Route1");
    assertThat(activity.date()).isEqualTo("2021-04-27");
    assertThat(activity.distance()).isEqualTo("34.68 km");
    assertThat(activity.duration()).isEqualTo("1:34:54");
    assertThat(activity.avgSpeed()).isEqualTo("21.93 km/h");
    assertThat(activity.temperature()).isEqualTo("/");
    assertThat(activity.totalAscent()).isEqualTo("/");
  }

  @Test
  @Order(3)
  void uploadDuplicateFile_ShouldFail() {
    File fitFile = new File("src/test/resources/testdata/2021-04-27_Route1.fit");

    given()
        .accept(MediaType.TEXT_HTML)
        .contentType(ContentType.MULTIPART)
        .multiPart("file", fitFile)
        .when()
        .post("/upload")
        .then()
        .log()
        .body()
        .statusCode(201)
        .body(containsString("duplicate-error"))
        .body(containsString("Activity already uploaded"));

    given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", YEAR)
        .when()
        .get("/stats")
        .then()
        .statusCode(200)
        .body("rides", equalTo(1));
  }

  @Test
  @Order(4)
  void recalculateActivity() {
    ActivityResponse activity = firstActivity(YEAR);

    given().when().put("/activities/id/" + activity.id()).then().statusCode(200);

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/activities/id/" + activity.id())
        .then()
        .statusCode(200)
        .body("id", equalTo(activity.id().toString()))
        .body("positions", not(empty()));
  }

  @Test
  @Order(5)
  void rateActivity() {
    ActivityResponse activity = firstActivity(YEAR);
    assertThat(activity.rate()).isZero(); // default rating

    // set rating to 3
    setRating(activity.id(), 3).body("rate", equalTo(3));

    // fetch activity and assert for persisted value 3
    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/activities/id/" + activity.id())
        .then()
        .statusCode(200)
        .body("rate", equalTo(3));

    // set rating to 3 again (toggle off)
    setRating(activity.id(), 3).body("rate", equalTo(0));

    // assert rating to be 0 (persisted)
    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/activities/id/" + activity.id())
        .then()
        .statusCode(200)
        .body("rate", equalTo(0));
  }

  @Test
  @Order(20)
  void addBikeToInventory() {
    given()
        .redirects()
        .follow(false)
        .contentType(ContentType.URLENC)
        .formParam("producer", "Canyon")
        .formParam("name", "Endurace")
        .when()
        .post("/bikes")
        .then()
        .statusCode(201);

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/bikes")
        .then()
        .statusCode(200)
        .body("[0].bike.producer", equalTo("Canyon"))
        .body("[0].bike.name", equalTo("Endurace"))
        .body("[0].totalDistance", equalTo("0.00 km"));
  }

  @Test
  @Order(21)
  void linkBikeToActivity() {
    UUID bikeId = firstBikeId();
    ActivityResponse activity = firstActivity(YEAR);

    given()
        .contentType(ContentType.JSON)
        .body("\"" + bikeId + "\"")
        .when()
        .put("/activities/id/%s/bike".formatted(activity.id()))
        .then()
        .statusCode(200)
        .body("bike.id", equalTo(bikeId.toString()));

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/bikes")
        .then()
        .statusCode(200)
        .body("[0].totalDistance", containsString("34.68 km"));
  }

  @Test
  @Order(22)
  void updateBikeInventory() {
    UUID bikeId = firstBikeId();

    given()
        .redirects()
        .follow(false)
        .contentType(ContentType.URLENC)
        .formParam("producer", "Canyon Updated")
        .formParam("name", "Endurace CF")
        .when()
        .put("/bikes/id/" + bikeId)
        .then()
        .statusCode(200);

    // explicit HTTP GET to verify changes are persisted
    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/bikes")
        .then()
        .statusCode(200)
        .body("[0].bike.producer", equalTo("Canyon Updated"))
        .body("[0].bike.name", equalTo("Endurace CF"))
        .body("[0].totalDistance", containsString("34.68 km"));
  }

  @Test
  @Order(100)
  void deleteBikeAndAssignments() {
    Bike bike = firstActivity(YEAR).bike();

    given().when().delete("/bikes/id/" + bike.id()).then().statusCode(204);

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/bikes")
        .then()
        .statusCode(200)
        .body("", hasSize(0));

    assertThat(firstActivity(YEAR).bike()).isNull();
  }

  @Test
  @Order(101)
  void deleteActivity() {
    UUID id = firstActivity(YEAR).id();

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/activities/id/" + id)
        .then()
        .statusCode(200);

    given().when().delete("/activities/id/" + id).then().statusCode(204);

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/activities/id/" + id)
        .then()
        .statusCode(404)
        .body("detail", equalTo("Activity with id '%s' does not exist".formatted(id)));

    // delete again -> 404
    given()
        .when()
        .delete("/activities/id/" + id)
        .then()
        .statusCode(404)
        .body("detail", equalTo("Activity with id '%s' does not exist".formatted(id)));
  }

  private static ActivityResponse firstActivity(int year) {
    return given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", year)
        .when()
        .get("/activities")
        .then()
        .statusCode(200)
        .body("", hasSize(1))
        .extract()
        .as(ActivityResponse[].class)[0];
  }

  private static UUID firstBikeId() {
    List<BikeResponse> bikes =
        given()
            .accept(MediaType.APPLICATION_JSON)
            .when()
            .get("/bikes")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    return bikes.getFirst().bike().id();
  }

  private static ValidatableResponse setRating(UUID activityId, int rate) {
    return given()
        .contentType(ContentType.JSON)
        .body(rate)
        .when()
        .put("/activities/id/%s/rate".formatted(activityId))
        .then()
        .statusCode(200);
  }
}
