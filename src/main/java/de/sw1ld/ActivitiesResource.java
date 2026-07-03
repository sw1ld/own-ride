package de.sw1ld;

import io.quarkiverse.httpproblem.HttpProblem;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/activities")
@RequestScoped
public class ActivitiesResource {

  private final ActivityService activityService;
  @Context private HttpHeaders headers;

  public ActivitiesResource(ActivityService activityService) {
    this.activityService = activityService;
  }

  @GET
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  public Response activities(@QueryParam("year") Integer year) {
    List<Activity> activities = activityService.fetchActivities(year);

    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      var years = activityService.getAvailableYears();

      return Response.ok(
              Templates.activities(
                  activities.stream()
                      .map(ActivityResponse::new)
                      .sorted(Comparator.comparing(ActivityResponse::date).reversed())
                      .toList(),
                  years))
          .build();
    } else {
      return Response.ok()
          .entity(
              activities.stream()
                  .map(ActivityResponse::new)
                  .sorted(Comparator.comparing(ActivityResponse::date).reversed())
                  .toList())
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .build();
    }
  }

  @GET
  @Path("/id/{id}")
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  public Response activity(@PathParam("id") UUID id) {
    Optional<Activity> activity = activityService.fetchActivityBy(id);

    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      if (activity.isEmpty()) {
        return Response.status(404).entity(Templates.notFound()).build();
      }
      return Response.ok(Templates.activity(new ActivityResponse(activity.get()))).build();
    } else {
      if (activity.isEmpty()) {
        return Response.status(404)
            .entity(notFoundProblem(id))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .build();
      }
      return Response.ok()
          .entity(new ActivityResponse(activity.get()))
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .build();
    }
  }

  @DELETE
  @Path("/id/{id}")
  public Response deleteActivity(@PathParam("id") UUID id) {
    return activityService.deleteActivity(id)
        ? Response.noContent().build()
        : Response.status(404).entity(notFoundProblem(id)).build();
  }

  @PUT
  @Path("/id/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response recalculate(@PathParam("id") UUID id) {
    Optional<Activity> activity = activityService.fetchActivityBy(id);
    if (activity.isEmpty()) {
      return Response.status(404).entity(notFoundProblem(id)).build();
    }
    return Response.ok()
        .entity(new ActivityResponse(activityService.recalculateActivity(id)))
        .build();
  }

  @PUT
  @Path("/id/{id}/rate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response rateActivity(@PathParam("id") UUID id, Integer rate) {
    Optional<Activity> activity = activityService.fetchActivityBy(id);
    if (activity.isEmpty()) {
      return Response.status(404).entity(notFoundProblem(id)).build();
    }
    Rate validRate;
    try {
      validRate = new Rate(rate);
    } catch (IllegalArgumentException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity(HttpProblem.valueOf(Status.BAD_REQUEST, e.getMessage()))
          .build();
    }

    return Response.ok()
        .entity(new ActivityResponse(activityService.setUserRating(id, validRate)))
        .build();
  }

  private static HttpProblem notFoundProblem(UUID id) {
    return HttpProblem.valueOf(
        Status.NOT_FOUND, "Activity with id '%s' does not exist".formatted(id));
  }
}
