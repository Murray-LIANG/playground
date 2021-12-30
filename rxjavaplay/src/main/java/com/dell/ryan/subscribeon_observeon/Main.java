package com.dell.ryan.subscribeon_observeon;


import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.util.Objects;
import java.util.Random;

public class Main {
  private static void s1SubscribeOnAndObserveOn() {
    Observable.just("long", "longer", "longest")
        .subscribeOn(Schedulers.io())
        .map(s -> {
          System.out.println("map item: " + s + " on thread: " + Thread.currentThread().getName());
          return s.length();
        })
        .observeOn(Schedulers.computation())
        .filter(item -> {
          System.out.println("filter item: " + item + " on thread: " + Thread.currentThread().getName());
          return item > 6;
        })
        .subscribe(integer -> System.out.println("subscribe item length: " + integer + " on thread: " + Thread.currentThread().getName()));
  }

  private static Observable<Integer> performLongOperation(String s) {
    Random random = new Random();
    try {
      Thread.sleep(random.nextInt(3) * 1000);
      return Observable.just(s.length());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static void s2SubscribeOnWithFlatMap() {
    // map above handles each item using the same thread sequentially preserving
    // the same order.

    // flatMap wraps each item being emitted by an Observable letting you apply
    // its own RxJava operators including assigning a new Scheduler using subscribeOn
    // to handle those operators. That is, each item emitted can be processed by
    // a separate thread simultaneously.

    Observable.just("long", "longer", "longest")
        .flatMap(s -> Objects.requireNonNull(performLongOperation(s))
            .doOnNext(item -> System.out.println("processing item: " + item + " on thread: " + Thread.currentThread().getName()))
            .subscribeOn(Schedulers.computation())
        ).subscribe(length -> System.out.println("subscribe item length: " + length + " on thread: " + Thread.currentThread().getName()));
  }

  private static void s3SubscribeOnWithConcatMap() {
    // In flatMap, the individual Observables are merged back into a single
    // Observable in no particular order.

    // concatMap guarantees that the order of the items processed is the same as
    // in the original emission.

    Observable.just("long", "longer", "longest")
        .concatMap(s -> Objects.requireNonNull(performLongOperation(s))
            .doOnNext(item -> System.out.println("processing item: " + item + " on thread: " + Thread.currentThread().getName()))
            .subscribeOn(Schedulers.computation())
        ).subscribe(length -> System.out.println("subscribe item length: " + length + " on thread: " + Thread.currentThread().getName()));

  }

  public static void main(String[] args) throws InterruptedException {
    s1SubscribeOnAndObserveOn();
    //s2SubscribeOnWithFlatMap();
    //s3SubscribeOnWithConcatMap();

    Thread.sleep(3000);
  }
}
