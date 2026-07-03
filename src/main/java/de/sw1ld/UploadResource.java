package de.sw1ld;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jspecify.annotations.NonNull;

@Path("/upload")
@RequestScoped
public class UploadResource {

  private final ActivityService activityService;
  @Context private HttpHeaders headers;

  public UploadResource(ActivityService activityService) {
    this.activityService = activityService;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response uploadPage() {
    return Response.ok(Templates.upload(null)).build();
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  public Response upload(MultipartFormDataInput fitfile) {
    var parts = fitfile.getFormDataMap().get("file");
    if (parts == null || parts.isEmpty()) {
      return Response.status(400)
          .entity(
              Templates.upload(
                  List.of(new UploadFailure("N/A", "Uploaded file must not be empty!"))))
          .build();
    }

    List<UploadResult> results = new ArrayList<>();
    for (var part : parts) {
      results.add(uploadSingleFile(part));
    }

    if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      return Response.status(Status.CREATED).entity(Templates.upload(results)).build();
    } else {
      return Response.status(Status.CREATED)
          .location(getFirstLocation(results))
          .entity(results)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .build();
    }
  }

  private static @NonNull URI getFirstLocation(List<UploadResult> results) {
    return results.stream()
        .filter(r -> r.result().equals(Result.SUCCESS))
        .map(r -> ((UploadSuccess) r).id())
        .map(id -> URI.create("/activities/id/%s".formatted(id)))
        .findFirst()
        .orElse(URI.create("/activities"));
  }

  private UploadResult uploadSingleFile(InputPart inputPart) {
    String fileName = inputPart.getFileName();
    if (fileName == null || fileName.isEmpty()) {
      return new UploadFailure("", "Filename must not be empty");
    }

    try {
      byte[] bytes = inputPart.getBody(byte[].class, null);
      if (bytes == null || bytes.length == 0) {
        return new UploadFailure(fileName, "File must not be empty");
      } else {
        UUID id = activityService.persistActivity(fileName, bytes);
        return new UploadSuccess(fileName, id);
      }
    } catch (Exception e) {
      return new UploadFailure(fileName, e.getMessage());
    }
  }
}
