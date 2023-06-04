package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

final public class JSON {

    private static final Gson gson = new Gson();

    synchronized public static String encode(Object obj) {
        return gson.toJson(obj);
    }

    synchronized public static <T> T decode(String json, Class<T> classOf) {
        return gson.fromJson(json, classOf);
    }

    synchronized public static <T> T decode(String key, TypeToken<?> typeOf) {
        return gson.fromJson(key, typeOf.getType());
    }

    @SuppressWarnings("unchecked")
    synchronized public static Map<String, Object> toMap(Object x) {
        return decode(encode(x), Map.class);
    }
}
