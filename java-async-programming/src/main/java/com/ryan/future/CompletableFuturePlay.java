package com.ryan.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class CompletableFuturePlay {
  public static void main(String[] args) throws InterruptedException, ExecutionException {
    // thenAccept vs. thenAcceptAsync
    System.out.println("The main thread: " + Thread.currentThread().getId());
    CompletableFuture.supplyAsync(() -> {
      try {
        System.out.println("Running asynchronous task in parallel in thread "
            + Thread.currentThread().getId());
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException ex) {
        throw new IllegalStateException(ex);
      }
      return "RYAN!!!";
    }).thenAccept(name -> System.out.println("Hello " + name + "in thread " + Thread.currentThread().getId())).get();
    CompletableFuture.supplyAsync(() -> {
      try {
        System.out.println("Running asynchronous task in parallel in thread " + Thread.currentThread().getId());
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException ex) {
        throw new IllegalStateException(ex);
      }
      return "RYAN!!!";
    }).thenAcceptAsync(name -> System.out.println("Hello " + name + "in thread " + Thread.currentThread().getId()))
        .get();

    // thenCompose vs thenApply
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello")
        .thenCompose(value -> CompletableFuture.supplyAsync(() -> value + " Ryan! Its thenCompose."));
    future.thenAccept(System.out::println).get();
    CompletableFuture<CompletableFuture<String>> future2 = CompletableFuture.supplyAsync(() -> "Hello")
        .thenApply(value -> CompletableFuture.supplyAsync(() -> value + " Ryan! Its thenApply."));
    System.out.println(future2.get().get());

    // Exception handling
    Integer age = -1;
    CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
      if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative.");
      }

      if (age > 18) {
        return "Adult";
      } else {
        return "Child";
      }
    }).exceptionally(ex -> {
      System.out.println("Oops! We have an exception - " + ex.getMessage());
      return "Unknown";
    });
    future3.thenAccept(System.out::println).get();

  }
}
