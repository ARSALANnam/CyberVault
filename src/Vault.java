import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.ArrayList;

class Vault {
    Path file;
    byte[] salt;
    SecretKey key;
    VaultData data;

    Vault() {
        file = Paths.get(System.getProperty("user.home"), ".cybervault", "vault.dat");
    }

    Vault(Path file) {
        this.file = file;
    }

    boolean exists() {
        return Files.exists(file);
    }

    void create(char[] master) throws Exception {
        salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        key = derive(master, salt);
        data = new VaultData();
        save();
    }

    boolean unlock(char[] master) throws Exception {
        byte[] raw = Files.readAllBytes(file);
        if (raw.length < 29) return false;
        salt = Arrays.copyOfRange(raw, 0, 16);
        key = derive(master, salt);
        try {
            byte[] dec = decrypt(key, Arrays.copyOfRange(raw, 16, raw.length));
            ObjectInputStream ois = new ObjectInputStream(new java.io.ByteArrayInputStream(dec));
            data = (VaultData) ois.readObject();
            for (PasswordEntry pe : data.passwords) if (pe.tags == null) pe.tags = new ArrayList<>();
            for (TokenEntry te : data.tokens) if (te.tags == null) te.tags = new ArrayList<>();
            ois.close();
            return true;
        } catch (Exception e) {
            key = null;
            data = null;
            return false;
        }
    }

    void save() throws Exception {
        Files.createDirectories(file.getParent());
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream s = new ObjectOutputStream(b);
        s.writeObject(data);
        s.close();
        byte[] enc = encrypt(key, b.toByteArray());
        byte[] out = new byte[16 + enc.length];
        System.arraycopy(salt, 0, out, 0, 16);
        System.arraycopy(enc, 0, out, 16, enc.length);
        Files.write(file, out);
    }

    void lock() {
        key = null;
        data = null;
        salt = null;
    }

    static SecretKey derive(char[] pass, byte[] salt) throws Exception {
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec ks = new PBEKeySpec(pass, salt, 120000, 256);
        return new SecretKeySpec(f.generateSecret(ks).getEncoded(), "AES");
    }

    static byte[] encrypt(SecretKey k, byte[] d) throws Exception {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, k, new GCMParameterSpec(128, iv));
        byte[] e = c.doFinal(d);
        byte[] out = new byte[12 + e.length];
        System.arraycopy(iv, 0, out, 0, 12);
        System.arraycopy(e, 0, out, 12, e.length);
        return out;
    }

    static byte[] decrypt(SecretKey k, byte[] d) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, k, new GCMParameterSpec(128, Arrays.copyOfRange(d, 0, 12)));
        return c.doFinal(Arrays.copyOfRange(d, 12, d.length));
    }
}
