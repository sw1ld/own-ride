package de.sw1ld;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
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
    when(activityService.fetchPerformanceData(anyInt()))
        .thenReturn(List.of(mockedPerformanceData()));

    StatisticResponse response =
        RestAssured.given()
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
  }

  @Test
  void statisticsPageAsHtml() {
    when(activityService.fetchPerformanceData(anyInt()))
        .thenReturn(List.of(mockedPerformanceData()));
    when(activityService.getAvailableYears()).thenReturn(List.of(2026));

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
            containsString("Total distance"),
            containsString("100.00 km"));
  }

  @Test
  void statisticsWithYearParam() {
    int year = 2025;
    when(activityService.fetchPerformanceData(year)).thenReturn(List.of());

    RestAssured.given()
        .queryParam("year", year)
        .when()
        .get(STATS_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("rides", org.hamcrest.Matchers.equalTo(0))
        .body("distance", org.hamcrest.Matchers.equalTo("0.00 km"));
  }

  private PerformanceData mockedPerformanceData() {
    return new PerformanceData(LocalDate.now(), 100.0, 222);
  }
}
