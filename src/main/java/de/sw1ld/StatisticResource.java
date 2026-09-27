package de.sw1ld;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

@Path("/stats")
@RequestScoped
public class StatisticResource {

  private final ActivityService activityService;
  @Context private HttpHeaders headers;

  public StatisticResource(ActivityService activityService) {
    this.activityService = activityService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response statistics(
      @QueryParam("filterUnit") String filterUnit, @QueryParam("offset") Integer offset) {
    LocalDate earliestActivityDate = activityService.getEarliestActivityDate();
    DateCalculator dateCalculator = new DateCalculator(filterUnit, offset, earliestActivityDate);

    List<PerformanceData> performanceData =
        activityService.fetchPerformanceData(dateCalculator.start(), dateCalculator.end());
    var stats = StatisticService.getStats(performanceData, dateCalculator);

    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      return Response.ok(Templates.statistics(stats)).build();
    } else {
      return Response.ok(stats).build();
    }
  }
}
