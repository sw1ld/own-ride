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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    when(activityService.fetchActivities(anyInt())).thenReturn(List.of(mockedActivity()));

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
    when(activityService.fetchActivities(anyInt())).thenReturn(List.of(mockedActivity()));
    when(activityService.getAvailableYears()).thenReturn(List.of(2026));

    given()
        .when()
        .accept(ContentType.HTML)
        .get(STATS_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(
            containsString("Statistics"),
            containsString("Summary"),
            containsString("Total number of rides"),
            containsString("100.00 km"));
  }

  @Test
  void statisticsWithYearParam() {
    int year = 2025;
    when(activityService.fetchActivities(year)).thenReturn(List.of());

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

  private Activity mockedActivity() {
    return new Activity(
        UUID.randomUUID(),
        "2026-07-16_Some_Name",
        LocalDate.now(),
        100.0,
        Duration.ofHours(2),
        Duration.ofHours(3),
        25.0,
        55.0,
        null,
        222,
        LocalDateTime.now(),
        3,
        List.of());
  }
}
