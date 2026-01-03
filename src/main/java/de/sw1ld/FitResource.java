package de.sw1ld;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Path("/fit")
@RequestScoped
public class FitResource {

  private final FitService fitService;

  public FitResource(FitService fitService) {
    this.fitService = fitService;
  }

  @GET
  @Path("/stats")
  @Produces(MediaType.TEXT_HTML)
  public Response index(@QueryParam("year") Integer year) {
    if (year == null) {
      year = LocalDate.now().getYear();
    }
    List<FitData> fitData = fitService.fetchDetails(year);

    return Response.ok(Templates.index(StatsService.getStats(fitData, year))).build();
  }

  @GET
  @Path("/stats")
  @Produces(MediaType.APPLICATION_JSON)
  public Response statistics(@QueryParam("year") Integer year) {
    if (year == null) {
      year = LocalDate.now().getYear();
    }
    List<FitData> fitData = fitService.fetchDetails(year);

    return Response.ok().entity(StatsService.getStats(fitData, year)).build();
  }

  @GET
  @Path("/details")
  @Produces(MediaType.APPLICATION_JSON)
  public Response allData(@QueryParam("year") Integer year) {
    List<FitData> fitData = fitService.fetchDetails(year);

    return Response.ok()
        .entity(
            fitData.stream()
                .map(FitResponse::new)
                .sorted(Comparator.comparing(FitResponse::date).reversed())
                .toList())
        .build();
  }

  @GET
  @Path("/details")
  @Produces(MediaType.TEXT_HTML)
  public Response details(@QueryParam("year") Integer year) {
    List<FitData> fitData = fitService.fetchDetails(year);

    return Response.ok(
            Templates.details(
                fitData.stream()
                    .map(FitResponse::new)
                    .sorted(Comparator.comparing(FitResponse::date).reversed())
                    .toList()))
        .build();
  }

  @GET
  @Path("details/name/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response dataByName(@PathParam("name") String name) {
    FitData fitData = fitService.fetchDetailsBy(name);

    return Response.ok().entity(new FitResponse(fitData)).build();
  }
}
