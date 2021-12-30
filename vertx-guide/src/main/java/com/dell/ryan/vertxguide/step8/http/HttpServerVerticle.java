package com.dell.ryan.vertxguide.step8.http;

import com.dell.ryan.vertxguide.step8.database.WikiDatabaseService;
import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class HttpServerVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
  public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

  private String wikiDbQueue = "wikidb.queue";

  private FreeMarkerTemplateEngine templateEngine;

  private WikiDatabaseService dbService;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue");

    dbService = WikiDatabaseService.createProxy(vertx.getDelegate(), wikiDbQueue);

    HttpServer httpServer = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.get("/").handler(this::indexHandler);
    router.get("/wiki/:page").handler(this::pageRenderingHandler);
    router.post().handler(BodyHandler.create());
    router.post("/save").handler(this::pageUpdateHandler);
    router.post("/create").handler(this::pageCreateHandler);
    router.post("/delete").handler(this::pageDeletionHandler);

    templateEngine = FreeMarkerTemplateEngine.create(vertx);

    int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
    httpServer.requestHandler(router)
        .listen(portNumber, ar -> {
          if (ar.succeeded()) {
            LOGGER.info("HTTP server running on port 8080");
            startPromise.complete();
          } else {
            LOGGER.error("Could not start a HTTP server", ar.cause());
            startPromise.fail(ar.cause());
          }
        });
  }

  private void indexHandler(RoutingContext routingContext) {

    dbService.fetchAllPages(reply -> {
      if (reply.succeeded()) {
        routingContext.put("title", "Wiki home");
        routingContext.put("pages", reply.result().getList());
        templateEngine.render(routingContext.data(), "templates/index.ftl", ar -> {
          if (ar.succeeded()) {
            routingContext.response().putHeader("Content-Type", "text/html");
            routingContext.response().end(ar.result());
          } else {
            routingContext.fail(ar.cause());
          }
        });
      } else {
        routingContext.fail(reply.cause());
      }
    });
  }

  private static final String EMPTY_PAGE_MARKDOWN =
      """
          # A new page

          Feel-free to write in Markdown!
          """;

  private void pageRenderingHandler(RoutingContext routingContext) {
    String page = routingContext.request().getParam("page");

    dbService.fetchPage(page, messageAsyncResult -> {
      if (messageAsyncResult.succeeded()) {
        JsonObject body = messageAsyncResult.result();

        boolean found = body.getBoolean("found");
        String rawContent = body.getString("rawContent", EMPTY_PAGE_MARKDOWN);

        routingContext.put("title", page);
        routingContext.put("id", body.getInteger("id", -1));
        routingContext.put("newPage", found ? "no" : "yes");
        routingContext.put("rawContent", rawContent);
        routingContext.put("content", Processor.process(rawContent));
        routingContext.put("timestamp", new Date().toString());

        templateEngine.render(routingContext.data(), "templates/page.ftl", ar -> {
          if (ar.succeeded()) {
            routingContext.response().putHeader("Content-Type", "text/html");
            routingContext.response().end(ar.result());
          } else {
            routingContext.fail(ar.cause());
          }
        });
      } else {
        routingContext.fail(messageAsyncResult.cause());
      }
    });
  }

  private void pageUpdateHandler(RoutingContext routingContext) {
    String title = routingContext.request().getParam("title");
    Handler<AsyncResult<Void>> handler = messageAsyncResult -> {
      if (messageAsyncResult.succeeded()) {
        routingContext.response().setStatusCode(303);
        routingContext.response().putHeader("Location", "/wiki/" + title);
        routingContext.response().end();
      } else {
        routingContext.fail(messageAsyncResult.cause());
      }
    };

    String markdown = routingContext.request().getParam("markdown");
    if ("yes".equals(routingContext.request().getParam("newPage"))) {
      dbService.createPage(title, markdown, handler);
    } else {
      dbService.savePage(Integer.parseInt(routingContext.request().getParam("id")), markdown, handler);
    }

  }

  private void pageCreateHandler(RoutingContext routingContext) {
    String pageName = routingContext.request().getParam("name");
    String location = "/wiki/" + pageName;
    if (pageName == null || pageName.isEmpty()) {
      location = "/";
    }

    routingContext.response().setStatusCode(303);
    routingContext.response().putHeader("Location", location);
    routingContext.response().end();
  }

  private void pageDeletionHandler(RoutingContext routingContext) {
    dbService.deletePage(Integer.parseInt(routingContext.request().getParam("id")),
        messageAsyncResult -> {
      if (messageAsyncResult.succeeded()) {
        routingContext.response().setStatusCode(303);
        routingContext.response().putHeader("Location", "/");
        routingContext.response().end();
      } else {
        routingContext.fail(messageAsyncResult.cause());
      }
    });
  }
}
