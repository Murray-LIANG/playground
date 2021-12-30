package com.ryan.vertxplay;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;


@RunWith(VertxUnitRunner.class)
public class SimpleServerVerticleUnitTest {

    private Vertx vertx;

    private int port = 8000;

    @Before
    public void setup(TestContext testContext) throws IOException {
        vertx = Vertx.vertx();

        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions().setConfig(
            new JsonObject().put("http.port", port));

        vertx.deployVerticle(SimpleServerVerticle.class.getName(), 
            options, testContext.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }
    
    @Test
    public void whenReceivedResponseThenSuccess(TestContext testContext) {
        final Async async = testContext.async();

        vertx.createHttpClient()
            .getNow(port, "localhost", "/", response -> response.handler(responseBody -> {
                testContext.assertTrue(responseBody.toString().contains("Welcome"));
                async.complete();
            }));
    }
}
