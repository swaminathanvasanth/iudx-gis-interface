package iudx.gis.server.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import iudx.gis.server.apiserver.response.ResponseUrn;
import iudx.gis.server.apiserver.util.HttpStatusCode;
import iudx.gis.server.configuration.Configuration;

import iudx.gis.server.database.postgres.PostgresServiceImpl;

import iudx.gis.server.database.postgres.PostgresService;
import iudx.gis.server.database.postgres.PostgresServiceImpl;
import iudx.gis.server.database.util.Constants;
import iudx.gis.server.database.util.Util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static iudx.gis.server.database.DatabaseServiceImpl.SELECT_GIS_SERVER_URL;
import static iudx.gis.server.database.util.Constants.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseServiceTest {
  @Mock
  DatabaseServiceImpl databaseService;
  private static DatabaseService database;

  private static String resId1;
  private static String resId2;
  private static String username;
  private static String password;
  private static int serverPort;
  public static String tokenUrl;
  private static String serverUrl;
  private static JsonObject accessInfo;

  /* Database Properties */
  private static String databaseIP;
  private static int databasePort;
  private static String databaseName;
  private static String databaseUserName;
  private static String databasePassword;
  private static int poolSize;
  private static PgConnectOptions connectOptions;
  private static PoolOptions poolOptions;
  private static PgPool pgPool;
  private static PostgresClient pgClient;

  private static Configuration dbConfig;

  private static final Logger LOGGER = LogManager.getLogger(DatabaseServiceTest.class);

  @BeforeAll
  @DisplayName("Deploy Database Verticle")
  static void startVertx(Vertx vertx, VertxTestContext vertxTestContext) {

    dbConfig = new Configuration();
    JsonObject config = dbConfig.configLoader(2, vertx);

    try {
      databaseIP = config.getString("databaseIP");
      databasePort = config.getInteger("databasePort");
      databaseName = config.getString("databaseName");
      databaseUserName = config.getString("databaseUserName");
      databasePassword = config.getString("databasePassword");
      poolSize = config.getInteger("dbClientPoolSize");
    } catch (Exception e) {
      LOGGER.fatal("Could not set db properties due to: {}", e.getMessage());
      vertxTestContext.failNow(e);
    }

    /* Set data for requests */
    resId1 = UUID.randomUUID().toString();
    resId2 = UUID.randomUUID().toString();
    username = UUID.randomUUID().toString();
    password = UUID.randomUUID().toString();
    serverUrl = UUID.randomUUID().toString();
    tokenUrl=UUID.randomUUID().toString();
    serverPort = ThreadLocalRandom.current().nextInt(1, 5000);
    accessInfo = new JsonObject()
            .put(USERNAME, username)
            .put(PASSWORD, password)
            .put(TOKEN_URL,tokenUrl);

    /* Set Connection Object */
    if (connectOptions == null) {
      connectOptions = new PgConnectOptions().setPort(databasePort).setHost(databaseIP)
              .setDatabase(databaseName).setUser(databaseUserName).setPassword(databasePassword);
    }

    /* Pool options */
    if (poolOptions == null) {
      poolOptions = new PoolOptions().setMaxSize(poolSize);
    }

    /* Create the client pool */
    pgPool = PgPool.pool(vertx, connectOptions, poolOptions);

    pgClient = new PostgresClient(vertx, connectOptions, poolOptions);
    database = new DatabaseServiceImpl(pgClient);

    vertxTestContext.completeNow();
  }

  @Test
  @DisplayName("Insert Admin Details into DB without accessInfo")
  @Order(1)
  void successfullyInsertAdminDetailsWithoutAccessInfo(VertxTestContext testContext) {
    JsonObject request = new JsonObject()
            .put(ID, resId1)
            .put(SERVER_URL, serverUrl)
            .put(SERVER_PORT, serverPort)
            .put(SECURE, false);

    JsonObject expected = new JsonObject()
            .put(DETAIL, SUCCESS);

    database.insertAdminDetails(request, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
        LOGGER.debug("Insert admin details without access info response is: {}", response.toString());
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Insert Admin Details into DB with accessInfo")
  @Order(2)
  void successfullyInsertAdminDetailsWithAccessInfo(VertxTestContext testContext) {
    JsonObject request = new JsonObject()
            .put(ID, resId2)
            .put(SERVER_URL, serverUrl)
            .put(SERVER_PORT, serverPort)
            .put(SECURE, true)
            .put(ACCESS_INFO, accessInfo);

    JsonObject expected = new JsonObject()
            .put(DETAIL, SUCCESS);

    database.insertAdminDetails(request, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
        LOGGER.debug("Insert admin details with access info response is: {}", response.toString());
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Update Admin Details without access info")
  @Order(3)
  void successfullyUpdateAdminDetailsWithoutAccessInfo(VertxTestContext testContext) {
    JsonObject request = new JsonObject()
            .put(ID, resId2)
            .put(SERVER_URL, serverUrl)
            .put(SERVER_PORT, serverPort)
            .put(SECURE, false);

    JsonObject expected = new JsonObject()
            .put(DETAIL, SUCCESS);

    database.updateAdminDetails(request, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
        LOGGER.debug("Update admin details without access info response is: {}", response.toString());
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Update Admin Details without access info")
  @Order(4)
  void successfullyUpdateAdminDetailsWithAccessInfo(VertxTestContext testContext) {
    JsonObject request = new JsonObject()
            .put(ID, resId1)
            .put(SERVER_URL, serverUrl)
            .put(SERVER_PORT, serverPort)
            .put(SECURE, true)
            .put(ACCESS_INFO, accessInfo);

    JsonObject expected = new JsonObject()
            .put(DETAIL, SUCCESS);

    database.updateAdminDetails(request, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
        LOGGER.debug("Update admin details with access info response is: {}", response.toString());
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Delete Admin Details case 1")
  @Order(5)
  void successfullyDeleteAdminDetails1(VertxTestContext testContext) {
    String request = resId1;

    JsonObject expected = new JsonObject()
            .put(DETAIL, SUCCESS);

    database.deleteAdminDetails(request, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
        LOGGER.debug("Delete admin details response is: {}", response.toString());
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Delete Admin Details case 2")
  @Order(6)
  void successfullyDeleteAdminDetails2(VertxTestContext testContext) {
    String request = resId2;

    JsonObject expected = new JsonObject()
            .put(DETAIL, SUCCESS);

    database.deleteAdminDetails(request, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
        LOGGER.debug("Delete admin details response is: {}", response.toString());
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  /*@Test
  @DisplayName("Execute Query Failed test case")
  @Order(7)
  public void executeQueryFailed(VertxTestContext testContext){
    PostgresServiceImpl postgresServiceImpl= new PostgresServiceImpl(pgPool);
    String query= "SELECT * FROM giss WHERE iudx_resource_id = '$1'";
    postgresServiceImpl.executeQuery(query,handler->{
      if (handler.succeeded())
      {
        testContext.failNow(handler.cause());
      }
      else
      {
        testContext.completeNow();
      }
    });
  }*/

  /*@Test
  @DisplayName("Execute Query Passed Case")
  @Order(8)
  public void excuteQueryPassedCase(VertxTestContext testContext){
    PostgresServiceImpl postgresService = new PostgresServiceImpl(pgPool);
    postgresService.executeQuery(SELECT_ADMIN_DETAILS_QUERY,handler->{
      if(handler.succeeded())
      {
        JsonObject response = handler.result();
        assertTrue(response.containsKey("result"));
        assertEquals(3,response.size());
        testContext.completeNow();
      }
      else {
        testContext.failNow(handler.cause());
      }
    });
  }*/

  @Test
  @DisplayName("Get URLIN DB Test")
  @Order(11)
  public void getURLInDbTest(VertxTestContext vertxTestContext){
    DatabaseServiceImpl databaseService1= new DatabaseServiceImpl(pgClient);
    databaseService1.getURLInDb(resId1);
    assertNotEquals(SELECT_GIS_SERVER_URL,databaseService1.getURLInDb(resId1));
    vertxTestContext.completeNow();
  }

  @Test
  @DisplayName("Search query test")
  @Order(11)
  public void searchQueryTest(VertxTestContext vertxTestContext){
    JsonObject jsonObject = new JsonObject().put(ID,"asdf");
    DatabaseServiceImpl databaseService1= new DatabaseServiceImpl(pgClient);
    databaseService1.searchQuery(jsonObject,h->{
      if (h.succeeded()){
        JsonObject response = h.result();
        assertEquals(1,response.size());
        vertxTestContext.completeNow();
      }
      else {
        vertxTestContext.failNow("failed");
      }
    });
  }

}
