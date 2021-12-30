package com.dell.ryan.vertxguide.step3;

import com.dell.ryan.vertxguide.step3.database.WikiDatabaseVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Promise<String> dbVerticleDeployment = Promise.promise();
    vertx.deployVerticle(new WikiDatabaseVerticle(), dbVerticleDeployment);

    dbVerticleDeployment.future().compose(id -> {
      Promise<String> httpVerticleDeployment = Promise.promise();
      vertx.deployVerticle(
          "com.dell.ryan.vertxguide.step3.http.HttpServerVerticle",
          new DeploymentOptions().setInstances(2),
          httpVerticleDeployment);
      return httpVerticleDeployment.future();
    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(ar.cause());
      }
    });
  }
}
