package com.dell.ryan.myrxjava;

import java.util.function.Function;

public class MapObservable<T, R> implements ObservableSource<R>{

  private final ObservableSource<T> upstream;
  private final Function<T, R> mapper;

  public MapObservable(ObservableSource<T> upstream, Function<T, R> mapper) {
    this.upstream = upstream;
    this.mapper = mapper;
  }

  @Override
  public void addObserver(Observer<R> downstream) {
    upstream.addObserver(new MapObserver<T, R>(downstream, mapper));
  }
}
