package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PasswordEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    public String title = "", username = "", password = "", url = "", notes = "";
    public List<String> tags = new ArrayList<>();
    public boolean favorite = false;
    public long created = System.currentTimeMillis();
}
