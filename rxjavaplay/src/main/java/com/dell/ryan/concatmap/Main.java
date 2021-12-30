package com.dell.ryan.concatmap;

import io.reactivex.Observable;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    // concatMap waits for each observable to finish all the work until next one
    // is processed.
    Observable.just("long", "longer", "longest")
        .doOnNext(s -> System.out.println("concatMap: got string: " + s
            + ", with length: " + s.length()))
        .concatMap(s ->
            Observable.just("concatMap: value=" + s + ",length=" + s.length())
                .delay(5, TimeUnit.SECONDS)
                .doOnNext(s1 -> System.out.println("concatMap: " + LocalDateTime.now()))
        )
        .doOnNext(System.out::println)
        .subscribe();

    Observable.just("long", "longer", "longest")
        .doOnNext(s -> System.out.println("flatMap: got string: " + s
            + ", with length: " + s.length()))
        .flatMap(s ->
            Observable.just("flatMap: value=" + s + ",length=" + s.length())
                .delay(5, TimeUnit.SECONDS)
                .doOnNext(s1 -> System.out.println("flatMap: " + LocalDateTime.now()))
        )
        .doOnNext(System.out::println)
        .subscribe();

    Thread.sleep(20000);
  }
}
