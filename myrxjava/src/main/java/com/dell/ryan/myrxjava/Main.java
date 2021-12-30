package com.dell.ryan.myrxjava;

public class Main {
  private static void TryObservableSource() {
    Observable.create(new ObservableSource<String>() {
      @Override
      public void addObserver(Observer<String> observer) {
        observer.onSubscribe();
      }
    }).subscribe(new Observer<String>() {
      @Override
      public void onSubscribe() {
        System.out.println("onSubscribe");
      }

      @Override
      public void onNext(String item) {
        System.out.println("onNext: " + item);
      }

      @Override
      public void onComplete() {
        System.out.println("onComplete");
      }

      @Override
      public void onError(Throwable e) {
        System.out.println("onError: " + e.getLocalizedMessage());
      }
    });
  }

  private static void TryIntListObservable() {
    Observable.create(new IntListObservable(1, 2, 3))
        .subscribe(new Observer<>() {
          @Override
          public void onSubscribe() {
            System.out.println("onSubscribe");
          }

          @Override
          public void onNext(Integer item) {
            System.out.println("onNext: " + item);
          }

          @Override
          public void onComplete() {
            System.out.println("onComplete");
          }

          @Override
          public void onError(Throwable e) {
            System.out.println("onError: " + e.getLocalizedMessage());
          }
        });
  }

  private static void TryIntListObservableThenMap() {
    Observable.create(new IntListObservable(1, 2,3))
        .map(integer -> "str" + integer.toString())
        .subscribe(new Observer<>() {
          @Override
          public void onSubscribe() {
            System.out.println("onSubscribe");
          }

          @Override
          public void onNext(String item) {
            System.out.println("onNext: " + item);
          }

          @Override
          public void onComplete() {
            System.out.println("onComplete");
          }

          @Override
          public void onError(Throwable e) {
            System.out.println("onError: " + e.getLocalizedMessage());
          }
        });
  }

  private static void TryIntListObservableThenSubscribeOn() {
    Observable.create(new IntListObservable(1,2,3))
        .map(integer -> {
          System.out.println("In thread: " + Thread.currentThread().getName());
          return "str_" + integer.toString();
        })
        .subscribeOn(Schedulers.THREAD_POOL)
        .map(s -> {
          System.out.println("In thread: " + Thread.currentThread().getName());
          return "boo_" + s;
        })
        .subscribe(new Observer<>() {
          @Override
          public void onSubscribe() {
            System.out.println("onSubscribe");
          }

          @Override
          public void onNext(String item) {
            System.out.println("onNext: " + item);
          }

          @Override
          public void onComplete() {
            System.out.println("onComplete");
          }

          @Override
          public void onError(Throwable e) {
            System.out.println("onError: " + e.getLocalizedMessage());
          }
        });
  }

  public static void main(String[] args) {
//    TryObservableSource();
//    TryIntListObservable();
//    TryIntListObservableThenMap();
    TryIntListObservableThenSubscribeOn();
  }
}
