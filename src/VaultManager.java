import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class VaultManager {
    final Path dir;
    final Path configFile;
    List<VaultInfo> vaults;
    String activeVaultName;
    String theme;
    Vault active;

    VaultManager() throws Exception {
        dir = Paths.get(System.getProperty("user.home"), ".cybervault");
        configFile = dir.resolve("config.json");
        Files.createDirectories(dir);
        loadConfig();
    }

    void loadConfig() throws Exception {
        if (!Files.exists(configFile)) {
            vaults = new ArrayList<>();
            activeVaultName = null;
            theme = "cyberpunk";
            saveConfig();
        } else {
            String json = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8);
            JsonObject obj = parseJson(json);
            vaults = new ArrayList<>();
            for (JsonObject v : obj.getArray("vaults")) {
                vaults.add(new VaultInfo(v.getString("name"), v.getString("file")));
            }
            activeVaultName = obj.getString("active", vaults.isEmpty() ? null : vaults.get(0).name);
            theme = obj.getString("theme", "cyberpunk");
        }
    }

    void saveConfig() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"vaults\": [\n");
        for (int i = 0; i < vaults.size(); i++) {
            VaultInfo v = vaults.get(i);
            sb.append("    {\"name\": ").append(jsonStr(v.name));
            sb.append(", \"file\": ").append(jsonStr(v.file)).append("}");
            if (i < vaults.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"active\": ").append(jsonStr(activeVaultName)).append(",\n");
        sb.append("  \"theme\": ").append(jsonStr(theme)).append("\n");
        sb.append("}\n");
        Files.write(configFile, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    VaultData createVault(String name, char[] master) throws Exception {
        String file = name.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".vault";
        for (VaultInfo v : vaults) if (v.file.equals(file)) throw new Exception("Vault file already exists");
        Vault vault = new Vault(dir.resolve(file));
        vault.create(master);
        vaults.add(new VaultInfo(name, file));
        activeVaultName = name;
        saveConfig();
        active = vault;
        return vault.data;
    }

    VaultData initExisting(String name, char[] master) throws Exception {
        VaultInfo info = null;
        for (VaultInfo v : vaults) if (v.name.equals(name)) { info = v; break; }
        if (info == null) throw new Exception("Vault not found");
        Vault vault = new Vault(dir.resolve(info.file));
        vault.create(master);
        activeVaultName = name;
        saveConfig();
        active = vault;
        return vault.data;
    }

    VaultData openVault(String name, char[] master) throws Exception {
        VaultInfo info = null;
        for (VaultInfo v : vaults) if (v.name.equals(name)) { info = v; break; }
        if (info == null) throw new Exception("Vault not found");
        Path file = dir.resolve(info.file);
        if (!Files.exists(file)) throw new Exception("Vault file missing");
        Vault vault = new Vault(file);
        if (!vault.unlock(master)) throw new Exception("Invalid master key");
        activeVaultName = name;
        saveConfig();
        active = vault;
        return vault.data;
    }

    void deleteVault(String name) throws Exception {
        VaultInfo info = null;
        for (VaultInfo v : vaults) if (v.name.equals(name)) { info = v; break; }
        if (info == null) return;
        Files.deleteIfExists(dir.resolve(info.file));
        vaults.remove(info);
        if (vaults.isEmpty()) activeVaultName = null;
        else if (name.equals(activeVaultName)) activeVaultName = vaults.get(0).name;
        active = null;
        saveConfig();
    }

    void renameVault(String oldName, String newName) throws Exception {
        for (VaultInfo v : vaults) if (v.name.equals(newName)) throw new Exception("Name exists");
        for (VaultInfo v : vaults) if (v.name.equals(oldName)) { v.name = newName; break; }
        if (oldName.equals(activeVaultName)) activeVaultName = newName;
        saveConfig();
    }

    void setTheme(String theme) throws Exception { this.theme = theme; saveConfig(); }

    List<String> getVaultNames() {
        List<String> names = new ArrayList<>();
        for (VaultInfo v : vaults) names.add(v.name);
        return names;
    }

    static JsonObject parseJson(String json) {
        json = json.trim();
        if (json.startsWith("{")) {
            JsonObject obj = new JsonObject();
            json = json.substring(1, json.length() - 1).trim();
            while (!json.isEmpty()) {
                int i = json.indexOf('"'); if (i < 0) break;
                int j = json.indexOf('"', i + 1);
                String key = json.substring(i + 1, j);
                json = json.substring(j + 1).trim();
                if (!json.startsWith(":")) break;
                json = json.substring(1).trim();
                Object val;
                if (json.startsWith("[")) {
                    int end = findMatch(json, '[', ']');
                    val = parseJsonArray(json.substring(1, end));
                    json = json.substring(end + 1).trim();
                } else if (json.startsWith("{")) {
                    int end = findMatch(json, '{', '}');
                    val = parseJson(json.substring(0, end + 1));
                    json = json.substring(end + 1).trim();
                } else if (json.startsWith("\"")) {
                    int end = json.indexOf('"', 1);
                    val = json.substring(1, end);
                    json = json.substring(end + 1).trim();
                } else {
                    int end = json.indexOf(','); if (end < 0) end = json.length();
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

    static List<JsonObject> parseJsonArray(String arr) {
        List<JsonObject> list = new ArrayList<>();
        arr = arr.trim();
        while (!arr.isEmpty()) {
            if (arr.startsWith("{")) {
                int end = findMatch(arr, '{', '}');
                list.add(parseJson(arr.substring(0, end + 1)));
                arr = arr.substring(end + 1).trim();
            } else break;
            if (arr.startsWith(",")) arr = arr.substring(1).trim();
        }
        return list;
    }

    static int findMatch(String s, char o, char c) {
        int d = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == o) d++; else if (ch == c) { d--; if (d == 0) return i; }
        }
        return s.length() - 1;
    }

    static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

class VaultInfo {
    String name, file;
    VaultInfo(String n, String f) { name = n; file = f; }
}

class JsonObject {
    Map<String, Object> map = new HashMap<>();
    String getString(String k) { return getString(k, null); }
    String getString(String k, String d) {
        Object v = map.get(k); return v instanceof String ? (String)v : d;
    }
    @SuppressWarnings("unchecked")
    List<JsonObject> getArray(String k) {
        Object v = map.get(k); return v instanceof List ? (List<JsonObject>)v : new ArrayList<>();
    }
}
