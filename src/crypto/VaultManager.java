package crypto;

import model.VaultData;
import model.VaultInfo;
import utils.JsonUtils;
import utils.JsonUtils.JsonObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VaultManager {
    public final Path dir;
    public final Path configFile;
    public List<VaultInfo> vaults;
    public String activeVaultName;
    public String theme;
    public Vault active;

    public VaultManager() throws Exception {
        dir = Paths.get(System.getProperty("user.home"), ".cybervault");
        configFile = dir.resolve("config.json");
        Files.createDirectories(dir);
        loadConfig();
    }

    public void migrateOldVault() throws Exception {
        Path old = dir.resolve("vault.dat");
        Path def = dir.resolve("default.vault");
        if (Files.exists(old) && !Files.exists(def) && !Files.exists(configFile)) {
            Files.copy(old, def);
        }
    }

    public void loadConfig() throws Exception {
        if (!Files.exists(configFile)) {
            vaults = new ArrayList<>();
            activeVaultName = null;
            theme = "cyberpunk";
            saveConfig();
        } else {
            String json = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8);
            JsonObject obj = JsonUtils.parseJson(json);
            vaults = new ArrayList<>();
            for (JsonObject v : obj.getArray("vaults")) {
                vaults.add(new VaultInfo(v.getString("name"), v.getString("file")));
            }
            activeVaultName = obj.getString("active", vaults.isEmpty() ? null : vaults.get(0).name);
            theme = obj.getString("theme", "cyberpunk");
        }
    }

    public void saveConfig() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"vaults\": [\n");
        for (int i = 0; i < vaults.size(); i++) {
            VaultInfo v = vaults.get(i);
            sb.append("    {\"name\": ").append(JsonUtils.jsonStr(v.name));
            sb.append(", \"file\": ").append(JsonUtils.jsonStr(v.file)).append("}");
            if (i < vaults.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"active\": ").append(JsonUtils.jsonStr(activeVaultName)).append(",\n");
        sb.append("  \"theme\": ").append(JsonUtils.jsonStr(theme)).append("\n");
        sb.append("}\n");
        Files.write(configFile, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public VaultData createVault(String name, char[] master) throws Exception {
        String file = name.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".vault";
        for (VaultInfo v : vaults) {
            if (v.file.equals(file)) {
                throw new Exception("Vault file already exists: " + file);
            }
        }
        Vault vault = new Vault(dir.resolve(file));
        vault.create(master);
        vaults.add(new VaultInfo(name, file));
        activeVaultName = name;
        saveConfig();
        active = vault;
        return active.data;
    }

    public VaultData initExisting(String name, char[] master) throws Exception {
        VaultInfo info = null;
        for (VaultInfo v : vaults) {
            if (v.name.equals(name)) { info = v; break; }
        }
        if (info == null) throw new Exception("Vault not found: " + name);
        Vault vault = new Vault(dir.resolve(info.file));
        vault.create(master);
        activeVaultName = name;
        saveConfig();
        active = vault;
        return vault.data;
    }

    public VaultData openVault(String name, char[] master) throws Exception {
        VaultInfo info = null;
        for (VaultInfo v : vaults) {
            if (v.name.equals(name)) { info = v; break; }
        }
        if (info == null) throw new Exception("Vault not found: " + name);
        Path file = dir.resolve(info.file);
        if (!Files.exists(file)) throw new Exception("Vault file missing: " + file);
        Vault vault = new Vault(file);
        if (!vault.unlock(master)) {
            throw new Exception("Invalid master key");
        }
        activeVaultName = name;
        saveConfig();
        active = vault;
        return active.data;
    }

    public void deleteVault(String name) throws Exception {
        if (vaults.size() <= 1) {
            throw new Exception("Cannot delete the last vault");
        }
        VaultInfo info = null;
        for (VaultInfo v : vaults) {
            if (v.name.equals(name)) { info = v; break; }
        }
        if (info == null) return;
        Files.deleteIfExists(dir.resolve(info.file));
        vaults.remove(info);
        if (activeVaultName.equals(name)) {
            activeVaultName = vaults.get(0).name;
        }
        active = null;
        saveConfig();
    }

    public void renameVault(String oldName, String newName) throws Exception {
        for (VaultInfo v : vaults) {
            if (v.name.equals(newName)) {
                throw new Exception("Vault name already exists: " + newName);
            }
        }
        for (VaultInfo v : vaults) {
            if (v.name.equals(oldName)) {
                v.name = newName;
                break;
            }
        }
        if (activeVaultName.equals(oldName)) {
            activeVaultName = newName;
        }
        saveConfig();
    }

    public void setTheme(String theme) throws Exception {
        this.theme = theme;
        saveConfig();
    }

    public List<String> getVaultNames() {
        List<String> names = new ArrayList<>();
        for (VaultInfo v : vaults) names.add(v.name);
        return names;
    }
}
