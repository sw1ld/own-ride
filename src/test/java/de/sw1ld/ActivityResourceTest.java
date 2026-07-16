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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ActivityResourceTest {

  private static final String ACTIVITIES_PATH = "activities";
  private static final String ACTIVITY_ID_PATH = ACTIVITIES_PATH + "/id/%s";

  private ActivityService activityService;

  @BeforeEach
  void setup() {
    activityService = mock(ActivityService.class);
    QuarkusMock.installMockForType(activityService, ActivityService.class);
  }

  @Test
  void fetchAllActivitiesDefaultingToJson() {
    when(activityService.fetchActivities(any())).thenReturn(List.of(mockedActivity()));

    List<ActivityResponse> response =
        RestAssured.given()
            .when()
            // .accept(ContentType.JSON)
            .get(ACTIVITIES_PATH)
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
              assertThat(element.displayName()).isEqualTo("Some Name");
              assertThat(element.distance()).isEqualTo("100.00 km");
              assertThat(element.avgSpeed()).isEqualTo("25.00 km/h");
              assertThat(element.temperature()).isEqualTo("/");
              assertThat(element.totalAscent()).isEqualTo("222 m");
              assertThat(element.rate()).isEqualTo(3);
            });
  }

  @Test
  void activitiesPageAsHtml() {
    when(activityService.fetchActivities(any())).thenReturn(List.of(mockedActivity()));

    RestAssured.given()
        .when()
        .accept(ContentType.HTML)
        .get(ACTIVITIES_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(
            containsString("Activities"), // header
            containsString("Details"), // sub header
            containsString("Average Speed"), // some table header
            containsString("25.00 km/h")); // some entry in table
  }

  @Test
  void activitiesByIdDefaultingToJson() {
    when(activityService.fetchActivityBy(any())).thenReturn(Optional.of(mockedActivity()));

    ActivityResponse response =
        given()
            .when()
            // .accept(ContentType.JSON)
            .get(ACTIVITY_ID_PATH.formatted(UUID.randomUUID()))
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .body()
            .as(ActivityResponse.class);

    assertThat(response.displayName()).isEqualTo("Some Name");
    assertThat(response.distance()).isEqualTo("100.00 km");
    assertThat(response.avgSpeed()).isEqualTo("25.00 km/h");
    assertThat(response.temperature()).isEqualTo("/");
    assertThat(response.totalAscent()).isEqualTo("222 m");
    assertThat(response.rate()).isEqualTo(3);
  }

  @Test
  void activityPageAsHtml() {
    when(activityService.fetchActivityBy(any())).thenReturn(Optional.of(mockedActivity()));

    given()
        .accept(MediaType.TEXT_HTML)
        .when()
        .get(ACTIVITY_ID_PATH.formatted(UUID.randomUUID()))
        .then()
        .statusCode(200)
        .body(
            containsString("Tour Information"),
            containsString("Elapsed Time"),
            containsString("55.00 km/h"),
            containsString("Altitude profile"));
  }

  @Test
  void activityNotFoundAsHtml() {
    when(activityService.fetchActivityBy(any())).thenReturn(Optional.empty());

    given()
        .accept(MediaType.TEXT_HTML)
        .when()
        .get(ACTIVITY_ID_PATH.formatted(UUID.randomUUID()))
        .then()
        .statusCode(404)
        .body(containsString("Activity Not Found"))
        .body(containsString("The activity you are looking for does not exist"))
        .body(containsString("Back to Activities"));
  }

  @Test
  void rateActivityWithInvalidRating_shouldFail() {
    when(activityService.fetchActivityBy(any())).thenReturn(Optional.of(mock(Activity.class)));

    given()
        .when()
        .contentType(ContentType.JSON)
        .body("7")
        .put(ACTIVITY_ID_PATH.formatted(UUID.randomUUID()) + "/rate")
        .then()
        .statusCode(400)
        .body(containsString("400"), containsString("Rate value must be between 0 and 5"));
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
