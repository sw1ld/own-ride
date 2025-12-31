package de.sw1ld;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/fit")
@RequestScoped
public class FitResource {

  private final FitService fitService;

  public FitResource(FitService fitService) {
    this.fitService = fitService;
  }

  @GET
  @Path("/index")
  @Produces(MediaType.TEXT_HTML)
  public Response index() {
    List<FitData> fitData = fitService.fetchData();

    return Response.ok(
            Templates.index(
                fitData.stream().map(FitResponse::new).toList(), StatsService.getStats(fitData)))
        .build();
  }

  @GET
  @Path("/data")
  @Produces(MediaType.APPLICATION_JSON)
  public Response allData() {
    List<FitData> fitData = fitService.fetchData();

    return Response.ok().entity(fitData.stream().map(FitResponse::new).toList()).build();
  }

  @GET
  @Path("data/name/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response dataByName(@PathParam("name") String name) {
    FitData fitData = fitService.fetchDataBy(name);

    return Response.ok().entity(new FitResponse(fitData)).build();
  }

  @GET
  @Path("/stats")
  @Produces(MediaType.APPLICATION_JSON)
  public Response statistics() {
    List<FitData> fitData = fitService.fetchData();

    return Response.ok().entity(StatsService.getStats(fitData)).build();
  }
}
