package com.dell.ryan.myrxjava;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public class Observable<T> {

  private final ObservableSource<T> source;

  private Observable(ObservableSource<T> source) {
    this.source = source;
  }

  public static <T> Observable<T> create(ObservableSource<T> source) {
    return new Observable<>(source);
  }

  public void subscribe(Observer<T> observer) {
    source.addObserver(observer);
  }

  public <R> Observable<R> map(Function<T, R> mapper) {
    return new Observable<R>(new MapObservable<T, R>(source, mapper));
  }

  public Observable<T> subscribeOn(ExecutorService executorService) {
    return new Observable<>(new SubscribeOnObservable<>(source, executorService));
  }
}
