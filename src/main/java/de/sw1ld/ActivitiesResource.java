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
  private final BikeService bikeService;
  @Context private HttpHeaders headers;

  public ActivitiesResource(ActivityService activityService, BikeService bikeService) {
    this.activityService = activityService;
    this.bikeService = bikeService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
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
          .build();
    }
  }

  @GET
  @Path("/id/{id}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response activity(@PathParam("id") UUID id) {
    Optional<Activity> activity = activityService.fetchActivityBy(id);

    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      if (activity.isEmpty()) {
        return Response.status(404).entity(Templates.notFound()).build();
      }
      List<Bike> bikes = bikeService.findAll();
      return Response.ok(Templates.activity(new ActivityResponse(activity.get()), bikes)).build();
    } else {
      if (activity.isEmpty()) {
        return Response.status(404).entity(notFoundProblem(id)).build();
      }
      return Response.ok().entity(new ActivityResponse(activity.get())).build();
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
    Optional<Activity> activity = activityService.recalculateActivity(id);
    if (activity.isEmpty()) {
      return Response.status(404).entity(notFoundProblem(id)).build();
    }

    return Response.ok().entity(new ActivityResponse(activity.get())).build();
  }

  @PUT
  @Path("/id/{id}/rate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response rateActivity(@PathParam("id") UUID id, Integer rate) {
    try {
      Optional<Activity> activity = activityService.setUserRating(id, rate);
      if (activity.isEmpty()) {
        return Response.status(404).entity(notFoundProblem(id)).build();
      }

      return Response.ok().entity(new ActivityResponse(activity.get())).build();
    } catch (IllegalRateException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity(HttpProblem.valueOf(Status.BAD_REQUEST, e.getMessage()))
          .build();
    }
  }

  @PUT
  @Path("/id/{id}/bike")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setBike(@PathParam("id") UUID id, UUID bikeId) {
    Optional<Activity> activity = activityService.linkBike(id, bikeId);
    if (activity.isEmpty()) {
      return Response.status(404).entity(notFoundProblem(id)).build();
    }

    return Response.ok().entity(new ActivityResponse(activity.get())).build();
  }

  @PUT
  @Path("/id/{id}/name")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setName(@PathParam("id") UUID id, String name) {
    Optional<Activity> activity = activityService.updateName(id, name);
    if (activity.isEmpty()) {
      return Response.status(404).entity(notFoundProblem(id)).build();
    }

    return Response.ok().entity(new ActivityResponse(activity.get())).build();
  }

  private static HttpProblem notFoundProblem(UUID id) {
    return HttpProblem.valueOf(
        Status.NOT_FOUND, "Activity with id '%s' does not exist".formatted(id));
  }
}
