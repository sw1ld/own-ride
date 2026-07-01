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
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  public Response statistics(@QueryParam("year") Integer year) {
    if (year == null) {
      year = LocalDate.now().getYear();
    }
    List<Activity> activities = activityService.fetchActivities(year);
    var stats = StatisticService.getStats(activities, year);

    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      var years = activityService.getAvailableYears();
      return Response.ok(Templates.statistics(stats, years)).build();
    } else {
      return Response.ok(stats).build();
    }
  }
}
