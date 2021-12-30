package com.dell.ryan.myrxjava;

import java.util.Arrays;
import java.util.List;

public class IntListObservable implements ObservableSource<Integer> {
  
  private final List<Integer> list;
  
  public IntListObservable(Integer a, Integer b, Integer c) {
    this.list = Arrays.asList(a, b, c);
  }

  @Override
  public void addObserver(Observer<Integer> observer) {
    observer.onSubscribe();

    this.list.forEach(observer::onNext);

    observer.onComplete();
  }
}
