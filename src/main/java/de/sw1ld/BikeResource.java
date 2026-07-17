package de.sw1ld;

import io.quarkiverse.httpproblem.HttpProblem;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Path("/bikes")
@RequestScoped
public class BikeResource {

  private final BikeService bikeService;
  @Context private HttpHeaders headers;

  public BikeResource(BikeService bikeService) {
    this.bikeService = bikeService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response bikes() {
    var bikes = bikeService.fetchBikes();
    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      return Response.ok(Templates.bikes(bikes)).build();
    } else {
      return Response.ok(bikes).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addBike(@FormParam("producer") String producer, @FormParam("name") String name) {
    BikeData bike = bikeService.addBike(producer, name);

    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      // seeOther to avoid "double submit problem" caused by page reload
      return Response.seeOther(URI.create("/bikes")).build();
    } else {
      return Response.status(Status.CREATED)
          .location(URI.create("/bikes/id/" + bike.getId()))
          .entity(new Bike(bike))
          .build();
    }
  }

  @GET
  @Path("/id/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response fetchBike(@PathParam("id") UUID id) {
    Optional<BikeData> bike = bikeService.findBike(id);

    return bike.isPresent()
        ? Response.ok(new Bike(bike.get())).build()
        : Response.status(Status.NOT_FOUND).entity(notFoundProblem(id)).build();
  }

  @PUT
  @Path("/id/{id}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateBike(
      @PathParam("id") UUID id,
      @FormParam("producer") String producer,
      @FormParam("name") String name) {
    BikeData bike = bikeService.updateBike(id, producer, name);

    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      // seeOther to avoid "double submit problem" caused by page reload
      return Response.seeOther(URI.create("/bikes")).build();
    } else {
      return Response.ok(bike).entity(new Bike(bike)).build();
    }
  }

  @DELETE
  @Path("/id/{id}")
  public Response deleteBike(@PathParam("id") UUID id) {
    bikeService.deleteBike(id);
    return Response.noContent().build();
  }

  private static HttpProblem notFoundProblem(UUID id) {
    return HttpProblem.valueOf(Status.NOT_FOUND, "Bike with id '%s' does not exist".formatted(id));
  }
}
