package hr.nipeta.util;

import lombok.extern.slf4j.Slf4j;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class JsonUtils {

    public static <T> T loadJson(String path, Function<JsonObject, T> parser) {
        try (InputStream is = JsonUtils.class.getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            JsonObject root = Json.createReader(is).readObject();
            return parser.apply(root);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }


    public static String serializeToJson(Map<String, Object> map) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        map.forEach((k, v) -> builder.add(k, v == null ? "" : v.toString()));
        return builder.build().toString();
    }

}
