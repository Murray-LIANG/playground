package com.dell.ryan.myrxjava;

public interface Observer<T> {
  void onSubscribe();
  void onNext(T item);
  void onComplete();
  void onError(Throwable e);
}
