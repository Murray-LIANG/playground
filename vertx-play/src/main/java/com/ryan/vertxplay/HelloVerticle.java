package com.ryan.vertxplay;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloVerticle.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HelloVerticle());
    }

    @Override
    public void start(Future<Void> future) {
        LOGGER.info("Starting hello server.");
        Router router = Router.router(Vertx.vertx());
        router.get("/hello").handler(ctx ->
            ctx.response()
                .putHeader("content-type", "text/html")
                .end("<html>hello</html>")
        );

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, ar -> {});
    }

    @Override
    public void stop() {
        LOGGER.info("Shutting down application");
    }

}
