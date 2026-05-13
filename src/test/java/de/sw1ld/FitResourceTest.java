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
class FitResourceTest {

  private static final int YEAR = 2021;

  @Test
  @Order(1)
  void initialApplication_hasNoContent() {
    given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", YEAR)
        .when()
        .get("/fit/stats")
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
        .get("/fit/details")
        .then()
        .statusCode(200)
        .body("", empty());

    UUID randomId = UUID.randomUUID();
    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/fit/details/id/" + randomId)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(2)
  void uploadFile() {
    File fitFile = new File("src/test/resources/testdata/2021-04-27_Route1.fit");

    given()
        .contentType(ContentType.MULTIPART)
        .multiPart("file", fitFile)
        .when()
        .post("/fit/upload")
        .then()
        .statusCode(200);

    given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", YEAR)
        .when()
        .get("/fit/stats")
        .then()
        .statusCode(200)
        .body("rides", equalTo(1))
        .body("distance", equalTo("34.68 km"))
        .body("tourDates.2021-01-01", equalTo(0.0F))
        .body("tourDates.2021-04-27", greaterThan(34.0F));

    FitResponse activity = firstActivity(YEAR);
    assertThat(activity.displayName()).isEqualTo("Route1");
    assertThat(activity.name()).isEqualTo("2021-04-27_Route1.fit");
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
        .post("/fit/upload")
        .then()
        .log()
        .body()
        .statusCode(200)
        .body(containsString("duplicate-error"))
        .body(containsString("Activity already uploaded"));

    given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", YEAR)
        .when()
        .get("/fit/stats")
        .then()
        .statusCode(200)
        .body("rides", equalTo(1));
  }

  @Test
  @Order(4)
  void recalculateActivity() {
    FitResponse activity = firstActivity(YEAR);

    given().when().put("/fit/details/id/" + activity.id()).then().statusCode(200);

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/fit/details/id/" + activity.id())
        .then()
        .statusCode(200)
        .body("id", equalTo(activity.id().toString()))
        .body("positions", not(empty()))
        .body("lastModified", not(equalTo(activity.lastModified().toString())));
  }

  @Test
  @Order(5)
  void rateActivity() {
    FitResponse activity = firstActivity(YEAR);

    // rate activity
    assertThat(activity.rate()).isZero(); // default rating

    // set rating to 3
    setRating(activity.id(), 3).body("rate", equalTo(3));

    // fetch activity and assert for persisted value 3
    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/fit/details/id/" + activity.id())
        .then()
        .statusCode(200)
        .body("rate", equalTo(3));

    // set rating to 3 again (toggle off)
    setRating(activity.id(), 3).body("rate", equalTo(0));

    // assert rating to be 0 (persisted)
    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/fit/details/id/" + activity.id())
        .then()
        .statusCode(200)
        .body("rate", equalTo(0));
  }

  @Test
  @Order(6)
  void deleteActivity() {
    UUID id = firstActivity(YEAR).id();

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/fit/details/id/" + id)
        .then()
        .statusCode(200);

    given().when().delete("/fit/details/id/" + id).then().statusCode(204);

    given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get("/fit/details/id/" + id)
        .then()
        .statusCode(404);

    // delete again -> 404
    given().when().delete("/fit/details/id/" + id).then().statusCode(404);
  }

  private static FitResponse firstActivity(int year) {
    return given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", year)
        .when()
        .get("/fit/details")
        .then()
        .statusCode(200)
        .body("", hasSize(1))
        .extract()
        .as(FitResponse[].class)[0];
  }

  private static ValidatableResponse setRating(UUID activityId, int rate) {
    return given()
        .contentType(ContentType.JSON)
        .body(rate)
        .when()
        .put("/fit/details/id/%s/rate".formatted(activityId))
        .then()
        .statusCode(200);
  }
}
