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
  private static final int PAGE_SIZE = 3;
  private static final int TOTAL_SIZE = 16;
  private static final ImmutableList<Integer> DATA =
      IntStream.range(0, TOTAL_SIZE).boxed().collect(ImmutableList.toImmutableList());

  public void testMain() {
    LOGGER.debug("Playground starts... in thread " + Thread.currentThread().getName());

    // Page fetching flowable
    Flowable<ImmutableList<Integer>> pageFlowStream =
        Flowable.generate(
            () -> 0,
            (state, emitter) -> {
              LOGGER.debug(
                  "Page reading item "
                      + state
                      + " ... in thread "
                      + Thread.currentThread().getName());
              int firstItemToRead = state;
              int lastDataElement = DATA.get(DATA.size() - 1);
              if (firstItemToRead > lastDataElement) {
                emitter.onComplete();
                return state;
              }
              int end =
                  (firstItemToRead + PAGE_SIZE) <= DATA.size()
                      ? (firstItemToRead + PAGE_SIZE)
                      : DATA.size();
              ImmutableList<Integer> pageRead = DATA.subList(firstItemToRead, end);
              emitter.onNext(pageRead);
              return end;
            });

    // Another flowable depending on the page read
    Flowable<Integer> dataComputeFlowStream =
        pageFlowStream
            .flatMap(Flowable::fromIterable)
            .map(
                dataElement -> {
                  LOGGER.debug(
                      "Computing item "
                          + dataElement
                          + " ... in thread "
                          + Thread.currentThread().getName());
                  return dataElement * dataElement * dataElement;
                });

    LOGGER.debug("Playground ends... in thread " + Thread.currentThread().getName());

    // Another flowable depending on the computng result
    Flowable<Integer> dataPostprocessFlowStream =
        dataComputeFlowStream.flatMap(
            dataElement -> {
              LOGGER.debug(
                  "Post processing item "
                      + dataElement
                      + " ... in thread "
                      + Thread.currentThread().getName());
              return Flowable.fromCallable(
                      () -> {
                        LOGGER.debug(
                            "Post processing item "
                                + dataElement
                                + " extra flow ... in thread "
                                + Thread.currentThread().getName());
                        return dataElement * -1;
                      })
                  .subscribeOn(Schedulers.io());
            },
            MAX_THREADS);

    dataPostprocessFlowStream
        .map(n -> n.toString())
        .subscribeOn(Schedulers.computation())
        .blockingSubscribe();
  }
}
