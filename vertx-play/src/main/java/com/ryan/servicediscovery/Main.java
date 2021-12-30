package com.ryan.servicediscovery;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.impl.DiscoveryImpl;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HelloVerticle());

        ServiceDiscovery discovery = new DiscoveryImpl(Vertx.vertx(),
            new ServiceDiscoveryOptions());

        Record record = HttpEndpoint.createRecord(
            "hello-service", "localhost", 8080, "/hello");

        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                System.out.println("Service published.");
            } else {
                System.out.println("Service failed to publish.");
            }
        });

        HttpEndpoint.getClient(discovery,
            new JsonObject().put("name", "hello-service"),
            ar -> {
                if (ar.succeeded()) {
                    HttpClient client = ar.result();
                    System.out.println("Client got: " + client);
                    client.getNow(
                        new RequestOptions(new JsonObject()).setHost("localhost").setPort(8080).setURI("/hello/x").addHeader("Content-Type", "application/json"),
//                        new RequestOptions(new JsonObject()).setPort(8080).setHost("localhost").setURI("/hello/x"),
                        response -> {
                            System.out.println(response.statusCode());
                            response.bodyHandler(body -> System.out.println(body.toString()));
                        });
                }
            }
        );
    }
}
