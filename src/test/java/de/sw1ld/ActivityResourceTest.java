package de.sw1ld;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkiverse.httpproblem.HttpProblem;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityResourceTest {

  @Mock ActivityService activityService;

  @InjectMocks ActivitiesResource cut;

  @Test
  void rateActivityWithInvalidRating_shouldFail() {
    when(activityService.fetchActivityBy(any())).thenReturn(Optional.of(mock(Activity.class)));

    Response response = cut.rateActivity(UUID.randomUUID(), 7);

    assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    assertThat(response.getEntity()).isInstanceOf(HttpProblem.class);
    assertThat(((HttpProblem) response.getEntity()).getDetail())
        .isEqualTo("Rate value must be between 0 and 5");
  }
}
