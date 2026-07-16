package de.sw1ld;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UploadResourceTest {

  private static final String UPLOAD_PATH = "upload";

  private ActivityService activityService;

  @BeforeEach
  void setup() {
    activityService = mock(ActivityService.class);
    QuarkusMock.installMockForType(activityService, ActivityService.class);
  }

  @Test
  void uploadPageAsHtml() {
    given()
        .when()
        .accept(MediaType.TEXT_HTML)
        .get(UPLOAD_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(containsString("File upload"), containsString("Select files"));
  }

  @Test
  void uploadDefaultingToJson() {
    UUID activityId = UUID.randomUUID();
    when(activityService.persistActivity(anyString(), any())).thenReturn(activityId);

    List<UploadSuccess> response =
        given()
            .multiPart("file", "test.fit", "some content".getBytes())
            .when()
            .post(UPLOAD_PATH)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .extract()
            .body()
            .as(new TypeRef<>() {});

    assertThat(response)
        .hasSize(1)
        .element(0)
        .satisfies(
            element -> {
              assertThat(element.fileName()).isEqualTo("test.fit");
              assertThat(element.id()).isEqualTo(activityId);
              assertThat(element.result()).isEqualTo(Result.SUCCESS);
            });
  }

  @Test
  void uploadResponseAsHtml() {
    UUID activityId = UUID.randomUUID();
    when(activityService.persistActivity(anyString(), any())).thenReturn(activityId);

    given()
        .multiPart("file", "test.fit", "some content".getBytes())
        .accept(MediaType.TEXT_HTML)
        .when()
        .post(UPLOAD_PATH)
        .then()
        .statusCode(201)
        .contentType(ContentType.HTML)
        .body(containsString("test.fit"), containsString("OK"));
  }
}
