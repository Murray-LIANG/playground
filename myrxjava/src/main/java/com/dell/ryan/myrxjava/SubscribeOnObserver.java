package com.dell.ryan.myrxjava;

public class SubscribeOnObserver<T> implements Observer<T> {
  private final Observer<T> downstream;

  public SubscribeOnObserver(Observer<T> downstream) {
    this.downstream = downstream;
  }

  @Override
  public void onSubscribe() {
    downstream.onSubscribe();
  }

  @Override
  public void onNext(T item) {
    downstream.onNext(item);
  }

  @Override
  public void onComplete() {
    downstream.onComplete();
  }

  @Override
  public void onError(Throwable e) {
    downstream.onError(e);
  }
}
