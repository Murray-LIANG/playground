import com.fasterxml.jackson.databind.JsonNode;

import java.util.Comparator;

public class VisibilityComparator implements Comparator<JsonNode> {
  private final int headerValue;
  public VisibilityComparator(int headerValue) {
    this.headerValue = headerValue;
  }

  @Override
  public int compare(JsonNode o1, JsonNode o2) {
    if (o1.equals(o2)) {
      return 0;
    }
    return 0;
  }
}
