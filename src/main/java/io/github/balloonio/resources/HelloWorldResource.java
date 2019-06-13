package io.github.balloonio.resources;

import com.codahale.metrics.annotation.Timed;
import io.github.balloonio.api.Saying;
import io.github.balloonio.core.Rxjava2Playground;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
  private final String template;
  private final String defaultName;
  private final AtomicLong counter;
  private static Logger LOGGER = LoggerFactory.getLogger(HelloWorldResource.class);

  public HelloWorldResource(String template, String defaultName) {
    this.template = template;
    this.defaultName = defaultName;
    this.counter = new AtomicLong();
  }

  @GET
  @Timed
  public Saying sayHello(@QueryParam("name") Optional<String> name) {
    final String value = String.format(template, name.orElse(defaultName));
    Rxjava2Playground rxjava2Playground = new Rxjava2Playground();
    rxjava2Playground.testMain();
    return new Saying(counter.incrementAndGet(), value);
  }

  void testPlayground() {
    LOGGER.debug("Playground starts... in thread " + Thread.currentThread().getName());

    Flowable.generate(
            () -> "hello world",
            (state, emitter) -> {
              LOGGER.debug("L45 in thread " + Thread.currentThread().getName());
              sleep(1000);
              emitter.onNext(state);
              return state + " ! ";
            })
        .map(
            str -> {
              LOGGER.debug("L51 in thread " + Thread.currentThread().getName());
              return str + " !!!";
            })
        .subscribeOn(Schedulers.computation())
        .map(
            str -> {
              LOGGER.debug("L56 in thread " + Thread.currentThread().getName());
              return str.toUpperCase();
            })
        .subscribe(
            str -> {
              LOGGER.debug("L55 in thread " + Thread.currentThread().getName());
              LOGGER.debug(str);
            });

    LOGGER.debug("Playground ends... in thread " + Thread.currentThread().getName());
  }
}
