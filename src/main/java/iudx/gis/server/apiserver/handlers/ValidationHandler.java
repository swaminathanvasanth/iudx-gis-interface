package iudx.gis.server.apiserver.handlers;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import iudx.gis.server.apiserver.util.RequestType;
import iudx.gis.server.apiserver.validation.ValidatorsHandlersFactory;
import iudx.gis.server.apiserver.validation.types.Validator;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ValidationHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LogManager.getLogger(ValidationHandler.class);
  private Vertx vertx;
  private RequestType type;


  public ValidationHandler(Vertx vertx, RequestType type) {
    this.vertx = vertx;
    this.type = type;
  }

  @Override
  public void handle(RoutingContext context) {
    ValidatorsHandlersFactory validationFactory = new ValidatorsHandlersFactory();
    MultiMap parameters = context.request().params();
    MultiMap headers = context.request().headers();
    Map<String, String> pathParams = context.pathParams();
    JsonObject body = context.getBodyAsJson();
    parameters.addAll(pathParams);
    List<Validator> validations = null;

    validations = validationFactory.build(vertx, type, parameters, headers, body);

    for (Validator validator : Optional.ofNullable(validations).orElse(Collections.emptyList())) {
      LOGGER.debug("validator :" + validator.getClass().getName());
      if (!validator.isValid()) {
        error(context);
        return;
      }
    }
    context.next();
    return;
  }

  private void error(RoutingContext context) {
    context.response().putHeader("content-type", "application/json")
        .setStatusCode(HttpStatus.SC_BAD_REQUEST).end(getBadRequestMessage().toString());
  }

  private JsonObject getBadRequestMessage() {
    return new JsonObject().put("type", 400).put("title", "Bad Request").put("details",
        "Bad query");
  }
}
