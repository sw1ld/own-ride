package de.sw1ld;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/upload")
@RequestScoped
public class UploadResource {

  private final ActivityService activityService;

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
            UUID id = activityService.persistActivity(fileName, bytes);
            results.add(UploadResult.success(fileName, id));
          }
        } catch (Exception e) {
          results.add(UploadResult.error(fileName, e.getMessage()));
        }
      }
    }

    return Response.ok(Templates.upload(results)).build();
  }
}
