package io.github.balloonio.core;

import com.google.common.collect.ImmutableList;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

public class Rxjava2Playground {
  private static final Logger LOGGER = LoggerFactory.getLogger(Rxjava2Playground.class);
  private static final int MAX_THREADS = 6;
  // mock 777 records of data
  // https://juejin.im/post/5b5ec33af265da0f845634df
  // nice reading: https://www.nurkiewicz.com/2017/09/idiomatic-concurrency-flatmap-vs.html
  private static final int PAGE_SIZE = 3;
  private static final int TOTAL_SIZE = 16;
  private static final ImmutableList<Integer> DATA =
      IntStream.range(0, TOTAL_SIZE).boxed().collect(ImmutableList.toImmutableList());

  public void testMain() {
    LOGGER.debug("Playground starts in T: " + Thread.currentThread().getName());

    // Page fetching flowable
    Flowable<ImmutableList<Integer>> pageFlowStream =
        Flowable.generate(
            () -> 0,
            (state, emitter) -> {
              LOGGER.debug(
                  "[pageFlowStream] Receive state "
                      + state
                      + " in T: "
                      + Thread.currentThread().getName());

              int firstItemToRead = state;
              int lastDataElement = DATA.get(DATA.size() - 1);
              if (firstItemToRead > lastDataElement) {
                emitter.onComplete();
                return state;
              }
              int end = Math.min((firstItemToRead + PAGE_SIZE), DATA.size());
              ImmutableList<Integer> pageRead = DATA.subList(firstItemToRead, end);
              LOGGER.debug(
                  "[pageFlowStream] Read items "
                      + firstItemToRead
                      + " ~ "
                      + end
                      + " in T: "
                      + Thread.currentThread().getName());
              emitter.onNext(pageRead);
              LOGGER.debug("[pageFlowStream] After onNext before return next state ");
              return end;
            });

    // Another flowable depending on the page read
    Flowable<Integer> dataComputeFlowStream =
        pageFlowStream
            .flatMap(Flowable::fromIterable)
            .map(
                dataElement -> {
                  LOGGER.debug(
                      "[dataComputeFlowStream] Computing item "
                          + dataElement
                          + " in T: "
                          + Thread.currentThread().getName());
                  return dataElement;
                });

    LOGGER.debug("Playground ends in T: " + Thread.currentThread().getName());

    // Another flowable depending on the computng result
    Flowable<Integer> dataPostprocessFlowStream =
        dataComputeFlowStream.flatMap(
            dataElement -> {
              LOGGER.debug(
                  "[dataPostprocessFlowStream1] Post processing item "
                      + dataElement
                      + " in T: "
                      + Thread.currentThread().getName());
              return Flowable.fromCallable(
                      () -> {
                        LOGGER.debug(
                            "[dataPostprocessFlowStream2] Post processing item "
                                + dataElement
                                + " extra flow in T: "
                                + Thread.currentThread().getName());
                        return dataElement * -1;
                      })
                  .subscribeOn(Schedulers.io());
            },
            MAX_THREADS);

    dataPostprocessFlowStream
        .map(
            n -> {
              LOGGER.debug("[String] " + n + " in T: " + Thread.currentThread().getName());
              return n.toString();
            })
        .subscribeOn(Schedulers.computation())
        .blockingSubscribe();
  }
}
