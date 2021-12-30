package com.dell.ryan.myrxjava;

public interface ObservableSource<T> {
  void addObserver(Observer<T> observer);

}
