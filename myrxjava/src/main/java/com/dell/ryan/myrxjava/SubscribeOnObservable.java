package com.dell.ryan.myrxjava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class SubscribeOnObservable<T> implements ObservableSource<T> {

  private final ObservableSource<T> upstream;
  private final ExecutorService executorService;
//  private Observer<T> downstream;

  public SubscribeOnObservable(ObservableSource<T> upstream, ExecutorService executorService) {
    this.upstream = upstream;
    this.executorService = executorService;
  }

  @Override
  public void addObserver(Observer<T> observer) {
    if (this.executorService == null) {
      this.upstream.addObserver(new SubscribeOnObserver<>(observer));
    } else {
      executorService.submit(() -> this.upstream.addObserver(new SubscribeOnObserver<>(observer)));
      executorService.shutdown();
      try {
        executorService.awaitTermination(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

//  @Override
//  public void addObserver(Observer<T> observer) {
//    this.downstream = observer;
//    if (this.executorService == null) {
//      this.upstream.addObserver(new SubscribeOnObserver<>(this.downstream));
//    } else {
//      executorService.submit(new addObserverTask());
//    }
//  }
//
//  private class addObserverTask implements Runnable {
//
//    @Override
//    public void run() {
//      upstream.addObserver(new SubscribeOnObserver<>(downstream));
//    }
//  }

}
