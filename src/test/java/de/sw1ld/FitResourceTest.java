package de.sw1ld;

import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FitResourceTest {

  private static final String BASE_PATH = "fit";
  private static final String FILENAME = "2025-08-17_file.fit";

  @InjectMock FitService fitService;

  @Test
  void fetchData() {
    when(fitService.fetchData())
        .thenReturn(
            List.of(
                new FitData(
                    FILENAME,
                    LocalDate.of(2025, 8, 17),
                    50.00,
                    Duration.ofSeconds(7886),
                    25.00,
                    38,
                    170,
                    List.of())));

    RestAssured.given()
        .when()
        .accept(ContentType.JSON)
        .get(BASE_PATH + "/data")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("[0].displayName", Matchers.equalTo("file"))
        .body("[0].name", Matchers.equalTo(FILENAME))
        .body("[0].date", Matchers.equalTo("2025-08-17"))
        .body("[0].distance", Matchers.equalTo("50.00 km"))
        .body("[0].duration", Matchers.equalTo("2:11:26"))
        .body("[0].totalAscent", Matchers.equalTo("170 m"))
        .body("[0].avgSpeed", Matchers.equalTo("25.00 km/h"))
        .body("[0].maxSpeed", Matchers.equalTo("38.00 km/h"));
  }

  @Test
  void fetchDistinct() {
    when(fitService.fetchDataBy("foobar.fit"))
        .thenReturn(
            new FitData(
                FILENAME,
                LocalDate.of(2025, 8, 17),
                60.00,
                Duration.ofSeconds(8112),
                20.00,
                38,
                170,
                List.of()));

    RestAssured.given()
        .when()
        .accept(ContentType.JSON)
        .get(BASE_PATH + "/data/name/foobar.fit")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("displayName", Matchers.equalTo("file"))
        .body("name", Matchers.equalTo(FILENAME))
        .body("date", Matchers.equalTo("2025-08-17"))
        .body("distance", Matchers.equalTo("60.00 km"))
        .body("totalAscent", Matchers.equalTo("170 m"))
        .body("avgSpeed", Matchers.equalTo("20.00 km/h"))
        .body("maxSpeed", Matchers.equalTo("38.00 km/h"));
  }

  @Test
  void fetchStatistics() {
    FitData fitData =
        new FitData(
            FILENAME,
            LocalDate.of(2025, 8, 17),
            50.00,
            Duration.ofSeconds(1159),
            25.00,
            38,
            170,
            List.of());
    when(fitService.fetchData()).thenReturn(List.of(fitData, fitData, fitData));

    RestAssured.given()
        .when()
        .accept(ContentType.JSON)
        .get(BASE_PATH + "/stats")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("rides", Matchers.equalTo(3))
        .body("distance", Matchers.equalTo("150.00 km"));
  }
}
