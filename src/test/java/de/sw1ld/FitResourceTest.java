package de.sw1ld;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
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

    given()
        .accept(MediaType.APPLICATION_JSON)
        .queryParam("year", YEAR)
        .when()
        .get("/fit/details")
        .then()
        .statusCode(200)
        .body("", hasSize(1))
        .body("[0].displayName", equalTo("Route1"))
        .body("[0].name", equalTo("2021-04-27_Route1.fit"))
        .body("[0].date", equalTo("2021-04-27"))
        .body("[0].distance", equalTo("34.68 km"))
        .body("[0].duration", equalTo("1:34:54"))
        .body("[0].avgSpeed", equalTo("21.93 km/h"))
        .body("[0].temperature", equalTo("/"))
        .body("[0].totalAscent", equalTo("/"))
        .body("[0].positions[0]", org.hamcrest.Matchers.hasKey("altitude"));
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
}
