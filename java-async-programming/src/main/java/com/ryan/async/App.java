package com.ryan.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Hello world!
 */
public final class App {
  private App() {
  }

  public static long factorial(int number) {
    long result = 1;
    for (int i = number; i > 0; i--) {
      result *= i;
    }
    return result;
  }

  /**
   * Says hello to the world.
   *
   * @param args The arguments of the program.
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, ExecutionException {
    int number = 20;

    long result = viaFutureTask(number);
    System.out.println("Factorial of " + number + " is: " + result);

    result = viaCompletableFuture(number);
    System.out.println("Factorial of " + number + " is: " + result);

    test(number);
  }

  public static long viaFutureTask(int number) throws InterruptedException, ExecutionException {

    ExecutorService threadPool = Executors.newCachedThreadPool();
    Future<Long> futureTask = threadPool.submit(() -> factorial(number));

    while (!futureTask.isDone()) {
      System.out.println("FutureTask is not finished yet...");
    }

    long result = futureTask.get();

    threadPool.shutdown();

    return result;
  }

  public static long viaCompletableFuture(int number)
          throws InterruptedException, ExecutionException {
    CompletableFuture<Long> completableFuture =
            CompletableFuture.supplyAsync(() -> factorial(number));
    return completableFuture.get();
  }

  public static void test(int number) {
    System.out.println("Started.");

    CompletableFuture.supplyAsync(() -> factorial(number)).thenApplyAsync(i -> i + 99)
            .thenAcceptAsync(i -> System.out.println("The result is: " + i))
            .thenRunAsync(() -> System.out.println("CompletableFuture finished."));

    System.out.println("Finished.");
  }
}
