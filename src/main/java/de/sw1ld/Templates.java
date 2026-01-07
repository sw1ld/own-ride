package de.sw1ld;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import java.util.List;

@CheckedTemplate
public class Templates {

  private Templates() {}

  public static native TemplateInstance statistics(StatisticResponse stats);

  public static native TemplateInstance details(List<FitResponse> data);

  public static native TemplateInstance upload(String message);
}
