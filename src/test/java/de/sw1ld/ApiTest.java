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
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
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
  }

  @Test
  @Order(2)
  void uploadFile() {
    File fitFile = new File("src/test/resources/testdata/2021-04-27_Route1.fit");

    given()
        .contentType(ContentType.MULTIPART)
        .accept(MediaType.APPLICATION_JSON)
        .multiPart("file", fitFile)
        .when()
        .post("/upload")
        .then()
        .statusCode(201);

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
  void activitiesPageHtml() {
    ActivityResponse activity = firstActivity(YEAR);

    given()
        .accept(MediaType.TEXT_HTML)
        .when()
        .get("/activities/id/" + activity.id())
        .then()
        .statusCode(200)
        .body(containsString(activity.displayName()))
        .body(containsString("Back to list"))
        .body(containsString("id=\"map\""))
        .body(containsString("id=\"altitudeChart\""));
  }

  @Test
  @Order(21)
  void activityNotFoundHtml() {
    UUID unknownId = UUID.randomUUID();

    given()
        .accept(MediaType.TEXT_HTML)
        .when()
        .get("/activities/id/" + unknownId)
        .then()
        .statusCode(404)
        .body(containsString("Activity Not Found"))
        .body(containsString("The activity you are looking for does not exist"))
        .body(containsString("Back to Activities"));
  }

  @Test
  @Order(100)
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
