package io.github.balloonio.resources;

import com.codahale.metrics.annotation.Timed;
import io.github.balloonio.api.Saying;
import io.github.balloonio.core.Rxjava2Playground;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
  private final String template;
  private final String defaultName;
  private final AtomicLong counter;

  public HelloWorldResource(String template, String defaultName) {
    this.template = template;
    this.defaultName = defaultName;
    this.counter = new AtomicLong();
  }

  @GET
  @Timed
  public Saying sayHello(@QueryParam("name") Optional<String> name) {
    final String value = String.format(template, name.orElse(defaultName));

    final Rxjava2Playground playground = new Rxjava2Playground();
    playground.testMain();

    return new Saying(counter.incrementAndGet(), value);
  }
}
