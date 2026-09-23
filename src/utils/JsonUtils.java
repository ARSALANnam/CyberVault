package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    public static class JsonObject {
        public Map<String, Object> map = new HashMap<>();

        public String getString(String key) { return getString(key, null); }

        public String getString(String key, String def) {
            Object v = map.get(key);
            return v instanceof String ? (String) v : def;
        }

        @SuppressWarnings("unchecked")
        public List<JsonObject> getArray(String key) {
            Object v = map.get(key);
            return v instanceof List ? (List<JsonObject>) v : new ArrayList<>();
        }
    }

    public static JsonObject parseJson(String json) {
        json = json.trim();
        if (json.startsWith("{")) {
            JsonObject obj = new JsonObject();
            json = json.substring(1, json.length() - 1).trim();
            while (!json.isEmpty()) {
                int i = json.indexOf('"');
                if (i < 0) break;
                int j = json.indexOf('"', i + 1);
                String key = json.substring(i + 1, j);
                json = json.substring(j + 1).trim();
                if (!json.startsWith(":")) break;
                json = json.substring(1).trim();
                Object val;
                if (json.startsWith("[")) {
                    int end = findMatchingBracket(json, '[', ']');
                    String arrStr = json.substring(1, end);
                    val = parseJsonArray(arrStr);
                    json = json.substring(end + 1).trim();
                } else if (json.startsWith("{")) {
                    int end = findMatchingBracket(json, '{', '}');
                    val = parseJson(json.substring(0, end + 1));
                    json = json.substring(end + 1).trim();
                } else if (json.startsWith("\"")) {
                    int end = json.indexOf('"', 1);
                    val = json.substring(1, end);
                    json = json.substring(end + 1).trim();
                } else {
                    int end = Math.min(json.indexOf(','), json.length());
                    if (end < 0) end = json.length();
                    val = json.substring(0, end).trim();
                    json = json.substring(end).trim();
                }
                if (json.startsWith(",")) json = json.substring(1).trim();
                obj.map.put(key, val);
            }
            return obj;
        }
        return new JsonObject();
    }

    public static List<JsonObject> parseJsonArray(String arr) {
        List<JsonObject> list = new ArrayList<>();
        arr = arr.trim();
        while (!arr.isEmpty()) {
            if (arr.startsWith("{")) {
                int end = findMatchingBracket(arr, '{', '}');
                list.add(parseJson(arr.substring(0, end + 1)));
                arr = arr.substring(end + 1).trim();
            } else {
                break;
            }
            if (arr.startsWith(",")) arr = arr.substring(1).trim();
        }
        return list;
    }

    public static int findMatchingBracket(String s, char open, char close) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return s.length() - 1;
    }

    public static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
