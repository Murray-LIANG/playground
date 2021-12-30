package com.dell.ryan.vertxguide.step1;

import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private static final String SQL_CREATE_PAGES_TABLE = "create table if not exists Pages (Id integer identity " +
      "primary key, Name varchar(255) unique, Content clob)";
  private static final String SQL_GET_PAGE = "select Id, Content from Pages where Name = ?";
  private static final String SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)";
  private static final String SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?";
  private static final String SQL_ALL_PAGES = "select Name from Pages";
  private static final String SQL_DELETE_PAGE = "delete from Pages where Id = ?";

  private JDBCClient dbClient;

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  private Future<Void> prepareDatabase() {
    Promise<Void> promise = Promise.promise();

    dbClient = JDBCClient.createShared(vertx,
        new JsonObject().put("url", "jdbc:hsqldb:file:db/wiki")
            .put("driver_class", "org.hsqldb.jdbcDriver")
            .put("max_pool_size", 30));

    dbClient.getConnection(ar -> {
      if (ar.failed()) {
        LOGGER.error("Could not open a database connection", ar.cause());
        promise.fail(ar.cause());
      } else {
        SQLConnection connection = ar.result();
        connection.execute(SQL_CREATE_PAGES_TABLE, create -> {
          connection.close();
          if (create.failed()) {
            LOGGER.error("Database preparation error", create.cause());
            promise.fail(create.cause());
          } else {
            promise.complete();
          }
        });
      }
    });

    return promise.future();
  }

  private FreeMarkerTemplateEngine templateEngine;

  private Future<Void> startHttpServer() {
    Promise<Void> promise = Promise.promise();
    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.get("/").handler(this::indexHandler);
    router.get("/wiki/:page").handler(this::pageRenderingHandler);
    router.post().handler(BodyHandler.create());
    router.post("/save").handler(this::pageUpdateHandler);
    router.post("/create").handler(this::pageCreateHandler);
    router.post("/delete").handler(this::pageDeletionHandler);

    templateEngine = FreeMarkerTemplateEngine.create(vertx);

    server.requestHandler(router)
        .listen(8080, ar -> {
          if (ar.succeeded()) {
            LOGGER.info("HTTP server running on port 8080");
            promise.complete();
          } else {
            LOGGER.error("Could not start a HTTP server", ar.cause());
            promise.fail(ar.cause());
          }
        });
    return promise.future();
  }

  private void indexHandler(RoutingContext routingContext) {
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.query(SQL_ALL_PAGES, res -> {
          connection.close();

          if (res.succeeded()) {
            List<String> pages = res.result().getResults()
                .stream()
                .map(json -> json.getString(0))
                .sorted()
                .collect(Collectors.toList());

            routingContext.put("title", "Wiki home");
            routingContext.put("pages", pages);
            templateEngine.render(routingContext.data(), "templates/index.ftl", ar -> {
              if (ar.succeeded()) {
                routingContext.response().putHeader("Content-Type", "text/html");
                routingContext.response().end(ar.result());
              } else {
                routingContext.fail(ar.cause());
              }
            });
          } else {
            routingContext.fail(res.cause());
          }
        });
      } else {
        routingContext.fail(car.cause());
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

    dbClient.getConnection(car -> {

      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.queryWithParams(SQL_GET_PAGE, new JsonArray().add(page),
            fetch -> {
              connection.close();
              if (fetch.succeeded()) {
                JsonArray row = fetch.result().getResults()
                    .stream()
                    .findFirst()
                    .orElseGet(() -> new JsonArray().add(-1).add(EMPTY_PAGE_MARKDOWN));

                Integer id = row.getInteger(0);
                String rawContent = row.getString(1);

                routingContext.put("title", page);
                routingContext.put("id", id);
                routingContext.put("newPage", fetch.result().getResults().size() == 0 ? "yes" : "no");
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
                routingContext.fail(fetch.cause());
              }
            });
      } else {
        routingContext.fail(car.cause());
      }
    });
  }

  private void pageUpdateHandler(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    String title = routingContext.request().getParam("title");
    String markdown = routingContext.request().getParam("markdown");
    boolean newPage = "yes".equals(routingContext.request().getParam("newPage"));

    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();

        String sql = newPage ? SQL_CREATE_PAGE : SQL_SAVE_PAGE;
        JsonArray params = new JsonArray();
        if (newPage) {
          params.add(title).add(markdown);
        } else {
          params.add(markdown).add(id);
        }

        connection.updateWithParams(sql, params, res -> {
          connection.close();

          if (res.succeeded()) {
            routingContext.response().setStatusCode(303);
            routingContext.response().putHeader("Location", "/wiki/" + title);
            routingContext.response().end();
          } else {
            routingContext.fail(res.cause());
          }
        });
      } else {
        routingContext.fail(car.cause());
      }
    });
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
    String id = routingContext.request().getParam("id");
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.updateWithParams(SQL_DELETE_PAGE, new JsonArray().add(id), res -> {
          connection.close();

          if (res.succeeded()) {
            routingContext.response().setStatusCode(303);
            routingContext.response().putHeader("Location", "/");
            routingContext.response().end();
          } else {
            routingContext.fail(res.cause());
          }
        });
      } else {
        routingContext.fail(car.cause());
      }
    });
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Future<Void> steps = prepareDatabase().compose(v -> startHttpServer());
    steps.setHandler(startPromise);
  }

  public void anotherStart(Promise<Void> startPromise) throws Exception {
    Future<Void> steps = prepareDatabase().compose(v -> startHttpServer());
    steps.setHandler(ar -> {
      if (ar.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(ar.cause());
      }
    });
  }
}
