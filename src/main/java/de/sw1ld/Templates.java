package de.sw1ld;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import java.util.List;

@CheckedTemplate
public class Templates {

  private Templates() {}

  public static native TemplateInstance statistics(
      StatisticResponse stats, List<Integer> availableYears);

  public static native TemplateInstance activities(
      List<ActivityResponse> data, List<Integer> availableYears);

  public static native TemplateInstance activity(ActivityResponse data);

  public static native TemplateInstance notFound();

  public static native TemplateInstance upload(List<UploadResult> results);
}
