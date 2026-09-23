import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class PasswordEntry implements Serializable {
    static final long serialVersionUID = 1L;
    String title = "", username = "", password = "", url = "", notes = "";
    List<String> tags = new ArrayList<>();
    boolean favorite = false;
    long created = System.currentTimeMillis();
}

class TokenEntry implements Serializable {
    static final long serialVersionUID = 1L;
    String name = "", token = "", notes = "";
    List<String> tags = new ArrayList<>();
    boolean favorite = false;
    long created = System.currentTimeMillis();
}

class VaultData implements Serializable {
    static final long serialVersionUID = 1L;
    List<PasswordEntry> passwords = new ArrayList<>();
    List<TokenEntry> tokens = new ArrayList<>();
}
