package de.sw1ld;

import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
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
    return Response.ok(Templates.upload(null, true)).build();
  }

  @Transactional
  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Response upload(MultipartFormDataInput fitfile) {
    // TODO verify/cut length of filename!
    var parts = fitfile.getFormDataMap().get("file");
    if (parts == null || parts.size() != 1 || parts.getFirst().getFileName().isEmpty()) {
      return Response.status(400)
          .entity(Templates.upload("Uploaded file must not be empty!", false))
          .build();
    }
    var part = parts.getFirst();
    try {
      UUID id = uploadService.persistActivity(part.getFileName(), part.getBody(byte[].class, null));

      return Response.ok(
              Templates.upload(
                  "Upload of [%s] was successful. Id [%s]".formatted(part.getFileName(), id), true))
          .build();
    } catch (Exception e) {
      return Response.ok(Templates.upload("Upload failed: " + e.getMessage(), false)).build();
    }
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
    FitData fitData = fitService.fetchDetailsBy(id);

    return Response.ok().entity(new FitResponse(fitData)).build();
  }
}
