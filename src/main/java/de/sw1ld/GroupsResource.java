package de.sw1ld;

import io.quarkiverse.httpproblem.HttpProblem;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Path("/groups")
@RequestScoped
public class GroupsResource {

  private final GroupService groupService;

  public GroupsResource(GroupService groupService) {
    this.groupService = groupService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createGroup(Set<UUID> activityIds) {
    if (activityIds.size() < 2) {
      return HttpProblem.valueOf(
              Status.BAD_REQUEST, "At least two activities are required to form a group.")
          .toResponse();
    }

    try {
      Group group = groupService.createGroup(activityIds);
      return Response.status(Status.CREATED).entity(new GroupResponse(group)).build();
    } catch (GroupingException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity(HttpProblem.valueOf(Status.BAD_REQUEST, e.getMessage()))
          .build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response fetchGroupsInRange(
      @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate) {
    LocalDate start = parseOrDefault(startDate, LocalDate.of(1900, Month.JANUARY, 1));
    LocalDate end = parseOrDefault(endDate, LocalDate.now());

    List<Group> groups = groupService.fetchGroups(start, end);

    return Response.ok().entity(groups.stream().map(g -> new GroupResponse(g)).toList()).build();
  }

  @GET
  @Path("/id/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response group(@PathParam("id") UUID id) {
    Optional<Group> group = groupService.fetchGroupBy(id);

    return group.isEmpty()
        ? Response.status(404).entity(notFoundProblem(id)).build()
        : Response.ok().entity(new GroupResponse(group.get())).build();
  }

  @PUT
  @Path("/id/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addActivityToGroup(@PathParam("id") UUID id, UUID activityId) {
    try {
      Optional<Group> group = groupService.addActivityToGroup(id, activityId);
      if (group.isEmpty()) {
        return Response.status(404).entity(notFoundProblem(id)).build();
      }

      return Response.ok().entity(new GroupResponse(group.get())).build();
    } catch (GroupingException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity(HttpProblem.valueOf(Status.BAD_REQUEST, e.getMessage()))
          .build();
    }
  }

  @DELETE
  @Path("/id/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response removeActivityFromGroup(@PathParam("id") UUID id, UUID activityId) {
    Optional<Boolean> dissolved = groupService.removeActivityFromGroup(id, activityId);
    if (dissolved.isEmpty()) {
      return Response.status(404).entity(notFoundProblem(id)).build();
    }

    if (dissolved.get()) {
      return Response.ok().header("X-Group-Dissolved", "true").build();
    }
    return Response.noContent().build();
  }

  @PUT
  @Path("/id/{id}/name")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setName(@PathParam("id") UUID id, String name) {
    Optional<Group> group = groupService.updateName(id, name);
    return group.isEmpty()
        ? Response.status(404).entity(notFoundProblem(id)).build()
        : Response.ok().entity(new GroupResponse(group.get())).build();
  }

  @PUT
  @Path("/id/{id}/rate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setRate(@PathParam("id") UUID id, Integer rate) {
    try {
      Optional<Group> group = groupService.setUserRating(id, rate);
      return group.isEmpty()
          ? Response.status(404).entity(notFoundProblem(id)).build()
          : Response.ok().entity(new GroupResponse(group.get())).build();
    } catch (IllegalRateException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity(HttpProblem.valueOf(Status.BAD_REQUEST, e.getMessage()))
          .build();
    }
  }

  private static HttpProblem notFoundProblem(UUID id) {
    return HttpProblem.valueOf(Status.NOT_FOUND, "Group with id '%s' does not exist".formatted(id));
  }

  private LocalDate parseOrDefault(String date, LocalDate defaultValue) {
    if (date == null) {
      return defaultValue;
    }
    try {
      return LocalDate.parse(date);
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
