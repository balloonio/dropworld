package io.github.balloonio;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.github.balloonio.resources.HelloWorldResource;

public class DropworldApplication extends Application<DropworldConfiguration> {

  public static void main(final String[] args) throws Exception {
    new DropworldApplication().run(args);
  }

  @Override
  public String getName() {
    return "Dropworld";
  }

  @Override
  public void initialize(final Bootstrap<DropworldConfiguration> bootstrap) {
    // TODO: application initialization
  }

  @Override
  public void run(final DropworldConfiguration configuration, final Environment environment) {
    // TODO: implement application
    final HelloWorldResource helloWorldResource =
        new HelloWorldResource(configuration.getTemplate(), configuration.getDefaultName());
    environment.jersey().register(helloWorldResource);
  }
}
