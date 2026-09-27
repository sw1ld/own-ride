package de.sw1ld;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StatisticResourceTest {

  private static final String STATS_PATH = "/stats";

  private ActivityService activityService;

  @BeforeEach
  void setup() {
    activityService = mock(ActivityService.class);
    QuarkusMock.installMockForType(activityService, ActivityService.class);
  }

  @Test
  void statisticsDefaultingToJson() {
    when(activityService.fetchPerformanceData(any(), any()))
        .thenReturn(List.of(mockedPerformanceData()));

    StatisticResponse response =
        RestAssured.given()
            .queryParam("filterUnit", "YEAR")
            .when()
            .get(STATS_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .body()
            .as(StatisticResponse.class);

    assertThat(response.rides()).isEqualTo(1);
    assertThat(response.distance()).isEqualTo("100.00 km");
    assertThat(response.tourDates()).hasSizeGreaterThan(363); // Full year initialized
    assertThat(response.tourDates()).containsEntry(LocalDate.now(), 100.0);
    assertThat(response.hasNext()).isFalse();
    assertThat(response.hasPrevious()).isFalse();
  }

  @Test
  void statisticsPageAsHtml() {
    when(activityService.fetchPerformanceData(any(), any()))
        .thenReturn(List.of(mockedPerformanceData()));

    given()
        .when()
        .accept(ContentType.HTML)
        .get(STATS_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(
            containsString("Dashboard"),
            containsString("Summary"),
            containsString("Total Distance"),
            containsString("100.00 km"));
  }

  @Test
  void statisticsWithYearFilterAndOffset() {
    when(activityService.fetchPerformanceData(any(), any())).thenReturn(List.of());

    RestAssured.given()
        .queryParam("filterUnit", "YEAR")
        .queryParam("offset", 1) // last calendar year
        .when()
        .get(STATS_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("rides", equalTo(0))
        .body("distance", equalTo("0.00 km"));
  }

  private PerformanceData mockedPerformanceData() {
    return new PerformanceData(
        UUID.randomUUID(), LocalDate.now(), 100.0, 222, Duration.ofHours(4), 22.0, 64.0);
  }
}
