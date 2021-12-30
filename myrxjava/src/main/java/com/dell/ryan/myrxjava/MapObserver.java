package com.dell.ryan.myrxjava;

import java.util.function.Function;

public class MapObserver<T, R> implements Observer<T> {
  private final Observer<R> downstream;
  private final Function<T, R> mapper;

  public MapObserver(Observer<R> downstream, Function<T,R> mapper) {
    this.downstream = downstream;
    this.mapper = mapper;
  }

  @Override
  public void onSubscribe() {
    this.downstream.onSubscribe();
  }

  @Override
  public void onNext(T item) {
    R result = mapper.apply(item);
    this.downstream.onNext(result);
  }

  @Override
  public void onComplete() {
    this.downstream.onComplete();
  }

  @Override
  public void onError(Throwable e) {
    this.downstream.onError(e);
  }
}
