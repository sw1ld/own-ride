package de.sw1ld;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/fit")
@RequestScoped
public class FitResource {

  private final FitService fitService;
  private final UploadService uploadService;

  public FitResource(FitService fitService, UploadService uploadService) {
    this.fitService = fitService;
    this.uploadService = uploadService;
  }

  @GET
  @Path("/upload")
  @Produces(MediaType.TEXT_HTML)
  public Response uploadPage() {
    return Response.ok(Templates.upload(null)).build();
  }

  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Response upload(MultipartFormDataInput fitfile) {
    var parts = fitfile.getFormDataMap().get("file");
    if (parts == null || parts.isEmpty()) {
      return Response.status(400)
          .entity(
              Templates.upload(
                  List.of(UploadResult.error("N/A", "Uploaded file must not be empty!"))))
          .build();
    }

    List<UploadResult> results = new ArrayList<>();
    for (var part : parts) {
      String fileName = part.getFileName();
      if (fileName != null && !fileName.isEmpty()) {
        try {
          byte[] bytes = part.getBody(byte[].class, null);
          if (bytes == null || bytes.length == 0) {
            results.add(UploadResult.error(fileName, "File is empty"));
          } else {
            UUID id = uploadService.persistActivity(fileName, bytes);
            results.add(UploadResult.success(fileName, id));
          }
        } catch (Exception e) {
          results.add(UploadResult.error(fileName, e.getMessage()));
        }
      }
    }

    return Response.ok(Templates.upload(results)).build();
  }

  @GET
  @Path("/stats")
  @Produces(MediaType.TEXT_HTML)
  public Response allStats(@QueryParam("year") Integer year) {
    if (year == null) {
      year = LocalDate.now().getYear();
    }
    List<FitData> fitData = fitService.fetchDetails(year);
    var stats = StatsService.getStats(fitData, year);
    var years = fitService.getAvailableYears();

    return Response.ok(Templates.statistics(stats, years)).build();
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
    var years = fitService.getAvailableYears();

    return Response.ok(
            Templates.details(
                fitData.stream()
                    .map(FitResponse::new)
                    .sorted(Comparator.comparing(FitResponse::date).reversed())
                    .toList(),
                years))
        .build();
  }

  @GET
  @Path("details/id/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response dataById(@PathParam("id") UUID id) {
    Optional<FitData> fitData = fitService.fetchDetailsBy(id);
    if (fitData.isEmpty()) {
      return Response.status(404).build();
    }
    return Response.ok().entity(new FitResponse(fitData.get())).build();
  }

  @DELETE
  @Path("details/id/{id}")
  public Response delete(@PathParam("id") UUID id) {
    return fitService.deleteActivity(id)
        ? Response.noContent().build()
        : Response.status(404).build();
  }

  @PUT
  @Path("details/id/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response recalculate(@PathParam("id") UUID id) {
    Optional<FitData> fitData = fitService.fetchDetailsBy(id);
    if (fitData.isEmpty()) {
      return Response.status(404).build();
    }
    return Response.ok().entity(new FitResponse(uploadService.recalculateActivity(id))).build();
  }

  @PUT
  @Path("details/id/{id}/rate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response rate(@PathParam("id") UUID id, Integer rate) {
    Optional<FitData> fitData = fitService.fetchDetailsBy(id);
    if (fitData.isEmpty()) {
      return Response.status(404).build();
    }
    Rate validRate;
    try {
      validRate = new Rate(rate);
    } catch (Exception e) {
      return Response.status(Status.BAD_REQUEST).build(); // TODO introduce HTTP Problem?
    }

    return Response.ok()
        .entity(new FitResponse(uploadService.setUserRating(id, validRate)))
        .build();
  }
}
