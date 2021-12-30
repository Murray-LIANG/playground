package com.ryan.servicediscovery;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloVerticle.class);

    @Override
    public void start(Future<Void> future) {
        LOGGER.info("Starting hello server.");
        System.out.println("Starting hello server.");
        Router router = Router.router(Vertx.vertx());
        router.errorHandler(HttpResponseStatus.NOT_FOUND.code(),
            ctx -> ctx.response().putHeader("content-type", "application/json").end("{}"))
            .get("/hello").handler(ctx -> {
            LOGGER.info("Get request");
            System.out.println("Get request");
            ctx.response()
                .putHeader("content-type", "text/html")
                .end("<html><h1>HELLO</h1></html>");
        });

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, ar -> {});
    }

    @Override
    public void stop() {
        LOGGER.info("Shutting down application");
    }

}
