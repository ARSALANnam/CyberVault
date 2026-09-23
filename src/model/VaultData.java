package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VaultData implements Serializable {
    private static final long serialVersionUID = 1L;

    public List<PasswordEntry> passwords = new ArrayList<>();
    public List<TokenEntry> tokens = new ArrayList<>();
}
