package com.dell.ryan.myrxjava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Schedulers {
//  public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
  public static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);
}
