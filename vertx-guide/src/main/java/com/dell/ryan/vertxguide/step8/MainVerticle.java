package com.dell.ryan.vertxguide.step8;

import com.dell.ryan.vertxguide.step8.http.HttpServerVerticle;
import com.dell.ryan.vertxguide.step8.database.WikiDatabaseVerticle;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
//    Promise<String> dbVerticleDeployment = Promise.promise();
//    vertx.deployVerticle(new WikiDatabaseVerticle(), dbVerticleDeployment);
//
//    dbVerticleDeployment.future().compose(id -> {
//      Promise<String> httpVerticleDeployment = Promise.promise();
//      vertx.deployVerticle(
//          "com.dell.ryan.vertxguide.step3.http.HttpServerVerticle",
//          new DeploymentOptions().setInstances(2),
//          httpVerticleDeployment);
//      return httpVerticleDeployment.future();
//    }).setHandler(ar -> {
//      if (ar.succeeded()) {
//        startPromise.complete();
//      } else {
//        startPromise.fail(ar.cause());
//      }
//    });

    vertx.rxDeployVerticle(new WikiDatabaseVerticle()).flatMap(id ->
      vertx.rxDeployVerticle(
          "com.dell.ryan.vertxguide.step8.http.HttpServerVerticle", new DeploymentOptions().setInstances(2))
          // new HttpServerVerticle())
    ).subscribe(id -> startPromise.complete(), startPromise::fail);

  }
}
