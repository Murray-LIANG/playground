import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
  public static void main(String[] args) throws IOException {

    Path visibilityJsonFile = Path.of("./src/main/resources/defined-visibility.json");
    String visibilityJsonStr = Files.readString(visibilityJsonFile);
    JsonObject visibilityJson = new JsonObject(visibilityJsonStr);

    Path requestJsonFile = Path.of("./src/main/resources/request.json");
    String requestJsonStr = Files.readString(requestJsonFile);
    JsonObject requestJson = new JsonObject(requestJsonStr);

    System.out.println(hasEnoughVisibility(requestJson, 0, visibilityJson));
  }

  public static boolean hasEnoughVisibility(JsonObject requestJson, int headerValue, JsonObject visibilityJson) {
    if (visibilityJson.containsKey("__MAX__") && headerValue >= visibilityJson.getInteger("__MAX__"))
      return true;

    for (String field: requestJson.fieldNames()) {
      if (!visibilityJson.containsKey(field)) {
        continue;
      }
      if (requestJson.getValue(field) instanceof JsonObject) {
        if (!hasEnoughVisibility(requestJson.getJsonObject(field), headerValue, visibilityJson.getJsonObject(field))) {
          return false;
        }
      } else {
        if (headerValue < visibilityJson.getInteger(field)) {
          return false;
        }
      }
    }
    return true;
  }
}
