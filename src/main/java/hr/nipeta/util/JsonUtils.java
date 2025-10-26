package hr.nipeta.util;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Map;

public class JsonUtils {
    public static String serializeToJson(Map<String, Object> map) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        map.forEach((k, v) -> builder.add(k, v == null ? "" : v.toString()));
        return builder.build().toString();
    }
}
