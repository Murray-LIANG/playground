package com.dell.ryan.play;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class Main {
  public static void main(String[] args) {
    //onErrorAndIgnore();
//    tryZip();
//    Flowable.just(1,2,3).single(999);
//    tryLastOrError();
//    singleJustNull();
    Single.just(null).doOnError(error -> System.out.println("ERRROR"))
        .subscribe(item -> System.out.println(item), error -> );
  }

  private static void onErrorAndIgnore() {
    Observable.just("a", "b", "c")
        .map(s -> {
          if (s.equals("b")) {
            throw new Exception("cannot be 'b'.");
          }
          return s;
        }).doOnError(err -> System.out.println("Caught an error: " + err))
        .ignoreElements()
        .onErrorComplete()
        .subscribe();
  }

  private static void tryZip() {
    Observable.zip(
        Observable.range(1, 5), Observable.range(6, 5), Observable::just
    ).flatMap(integerObservable -> integerObservable)
        .doOnNext(System.out::println)
        .subscribe();
  }

  private static void tryLastOrError() {
    Flowable.just(2)
        .filter(integer -> integer % 2 == 1)
        .lastOrError()
        .subscribe(
            integer -> System.out.println("OK: " + integer),
            error -> System.out.println("Error: " + error));
  }

  private static void singleJustNull() {
    Single.fromCallable(() -> null)
        .doOnSuccess(item -> System.out.println("item: " + item))
        .flatMap(item -> Single.just(1))
        .doOnError(error -> System.out.println("blabla: " + error))
        .subscribe(
            integer -> System.out.println("OK: " + integer),
            error -> System.out.println("Error: " + error));
  }
}
