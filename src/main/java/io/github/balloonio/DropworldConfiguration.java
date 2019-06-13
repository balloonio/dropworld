package io.github.balloonio;

import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.*;
import javax.validation.constraints.*;

public class DropworldConfiguration extends Configuration {
  // TODO: implement service configuration
  @NotEmpty private String template;

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  @NotEmpty private String defaultName;

  public String getDefaultName() {
    return defaultName;
  }

  public void setDefaultName(String defaultName) {
    this.defaultName = defaultName;
  }
}
