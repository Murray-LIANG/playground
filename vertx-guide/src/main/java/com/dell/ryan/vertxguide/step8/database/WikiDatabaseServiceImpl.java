package com.dell.ryan.vertxguide.step8.database;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClientHelper;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class WikiDatabaseServiceImpl implements WikiDatabaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(WikiDatabaseServiceImpl.class);

  private final JDBCClient jdbcClient;
  private final HashMap<SqlQuery, String> sqlQueries;

  public WikiDatabaseServiceImpl(JDBCClient jdbcClient, HashMap<SqlQuery, String> sqlQueries,
      Handler<AsyncResult<WikiDatabaseService>> readyHandler) {

    this.jdbcClient = jdbcClient;
    this.sqlQueries = sqlQueries;

//    jdbcClient.getConnection(ar -> {
//      if (ar.failed()) {
//        LOGGER.error("Could not open a database connection", ar.cause());
//        readyHandler.handle(Future.failedFuture(ar.cause()));
//      } else {
//        SQLConnection connection = ar.result();
//        connection.execute(sqlQueries.get(SqlQuery.CREATE_PAGES_TABLE), create -> {
//          connection.close();
//          if (create.failed()) {
//            LOGGER.error("Database preparation error", create.cause());
//            readyHandler.handle(Future.failedFuture(create.cause()));
//          } else {
//            readyHandler.handle(Future.succeededFuture(this));
//          }
//        });
//      }
//    });

    SQLClientHelper.usingConnectionSingle(this.jdbcClient,
        sqlConnection -> sqlConnection.rxExecute(sqlQueries.get(SqlQuery.CREATE_PAGES_TABLE))
            .andThen(Single.just(this))
    ).subscribe(SingleHelper.toObserver(readyHandler));

    jdbcClient.rxGetConnection().flatMap(sqlConnection -> {
      Single<SQLConnection> connectionSingle = Single.just(sqlConnection);
      return connectionSingle.doFinally(sqlConnection::close);
    }).flatMap(sqlConnection -> sqlConnection.rxExecute(sqlQueries.get(SqlQuery.CREATE_PAGES_TABLE))
        .andThen(Single.just(this)))
        .subscribe(SingleHelper.toObserver(readyHandler));
  }

  @Override
  public WikiDatabaseService fetchAllPages(Handler<AsyncResult<JsonArray>> resultHandler) {
//    jdbcClient.query(sqlQueries.get(SqlQuery.ALL_PAGES), res -> {
//      if (res.succeeded()) {
//        JsonArray pages = new JsonArray(res.result()
//            .getResults()
//            .stream()
//            .map(json -> json.getString(0))
//            .sorted()
//            .collect(Collectors.toList()));
//        resultHandler.handle(Future.succeededFuture(pages));
//      } else {
//        LOGGER.error("Database query error", res.cause());
//        resultHandler.handle(Future.failedFuture(res.cause()));
//      }
//    });

    jdbcClient.rxQuery(sqlQueries.get(SqlQuery.ALL_PAGES))
        .flatMapPublisher(resultSet -> {
          List<JsonArray> results = resultSet.getResults();
          return Flowable.fromIterable(results);
        }).map(json -> json.getString(0))
        .sorted()
        .collect(JsonArray::new, JsonArray::add)
        .subscribe(SingleHelper.toObserver(resultHandler));
    return this;
  }

  @Override
  public WikiDatabaseService fetchPage(String name, Handler<AsyncResult<JsonObject>> resultHandler) {
//    jdbcClient.queryWithParams(sqlQueries.get(SqlQuery.GET_PAGE), new JsonArray().add(name), fetch -> {
//      if (fetch.succeeded()) {
//        JsonObject response = new JsonObject();
//        ResultSet resultSet = fetch.result();
//        if (resultSet.getNumRows() == 0) {
//          response.put("found", false);
//        } else {
//          response.put("found", true);
//          JsonArray row = resultSet.getResults().get(0);
//          response.put("id", row.getInteger(0));
//          response.put("rawContent", row.getString(1));
//        }
//        resultHandler.handle(Future.succeededFuture(response));
//      } else {
//        LOGGER.error("Database query error", fetch.cause());
//        resultHandler.handle(Future.failedFuture(fetch.cause()));
//      }
//    });

    jdbcClient.rxQueryWithParams(sqlQueries.get(SqlQuery.GET_PAGE), new JsonArray().add(name))
        .map(resultSet -> {
          if (resultSet.getNumRows() > 0) {
            JsonArray row = resultSet.getResults().get(0);
            return new JsonObject().put("found", true)
                .put("id", row.getInteger(0))
                .put("rawContent", row.getString(1));
          } else {
            return new JsonObject().put("found", false);
          }
        }).subscribe(SingleHelper.toObserver(resultHandler));
    return this;
  }

  @Override
  public WikiDatabaseService createPage(String title, String markdown, Handler<AsyncResult<Void>> resultHandler) {
    JsonArray data = new JsonArray().add(title).add(markdown);
//    jdbcClient.updateWithParams(sqlQueries.get(SqlQuery.CREATE_PAGE), data, res -> {
//      if (res.succeeded()) {
//        resultHandler.handle(Future.succeededFuture());
//      } else {
//        LOGGER.error("Database update error", res.cause());
//        resultHandler.handle(Future.failedFuture(res.cause()));
//      }
//    });

    jdbcClient.rxUpdateWithParams(sqlQueries.get(SqlQuery.CREATE_PAGE), data)
        .ignoreElement()
        .subscribe(CompletableHelper.toObserver(resultHandler));
    return this;
  }

  @Override
  public WikiDatabaseService savePage(int id, String markdown, Handler<AsyncResult<Void>> resultHandler) {
    JsonArray data = new JsonArray().add(markdown).add(id);
//    jdbcClient.updateWithParams(sqlQueries.get(SqlQuery.SAVE_PAGE), data, res -> {
//      if (res.succeeded()) {
//        resultHandler.handle(Future.succeededFuture());
//      } else {
//        LOGGER.error("Database update error", res.cause());
//        resultHandler.handle(Future.failedFuture(res.cause()));
//      }
//    });

    jdbcClient.rxUpdateWithParams(sqlQueries.get(SqlQuery.SAVE_PAGE), data)
        .ignoreElement()
        .subscribe(CompletableHelper.toObserver(resultHandler));
    return this;
  }

  @Override
  public WikiDatabaseService deletePage(int id, Handler<AsyncResult<Void>> resultHandler) {
    JsonArray data = new JsonArray().add(id);
//    jdbcClient.updateWithParams(sqlQueries.get(SqlQuery.DELETE_PAGE), data, res -> {
//      if (res.succeeded()) {
//        resultHandler.handle(Future.succeededFuture());
//      } else {
//        LOGGER.error("Database update error", res.cause());
//        resultHandler.handle(Future.failedFuture(res.cause()));
//      }
//    });
    jdbcClient.rxUpdateWithParams(sqlQueries.get(SqlQuery.DELETE_PAGE), data)
        .ignoreElement()
        .subscribe(CompletableHelper.toObserver(resultHandler));
    return this;
  }
}
