import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSliderUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.Box;


public class CyberVault extends JFrame {

    /* THEME */
    static Color BG = new Color(0x0A0A14);
    static Color BG_PANEL = new Color(0x10101E);
    static Color BG_CARD = new Color(0x151528);
    static Color BG_FIELD = new Color(0x0C0C1A);
    static Color LINE = new Color(0x2A2F4A);
    static Color NEON_CYAN = new Color(0x00F0FF);
    static Color NEON_PINK = new Color(0xFF2A6D);
    static Color NEON_PURP = new Color(0x9D4EFF);
    static Color NEON_GRN = new Color(0x39FF14);
    static Color NEON_YEL = new Color(0xFFE600);
    static Color TXT = new Color(0xE4E9FF);
    static Color TXT_DIM = new Color(0x7A82A8);
    static Color BG_GRAD = new Color(0x16, 0x0B, 0x26);
    static Color DIM_1 = new Color(0x555C82);
    static Color DIM_2 = new Color(0x454B6E);
    static Color SCROLL_C = new Color(0x333A5C);
    static boolean matrixRain = false;

    static Font pickMono(int style, float size) {
        String[] prefs = {"Consolas", "JetBrains Mono", "Cascadia Code", "Fira Code", "Menlo", "DejaVu Sans Mono"};
        Set<String> avail = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String p : prefs) if (avail.contains(p)) return new Font(p, style, 12).deriveFont(size);
        return new Font(Font.MONOSPACED, style, 12).deriveFont(size);
    }

    static final Font F_MONO   = pickMono(Font.PLAIN, 13f);
    static final Font F_MONO_S = pickMono(Font.PLAIN, 11f);
    static final Font F_MONO_B = pickMono(Font.BOLD, 13f);
    static final Font F_TITLE  = pickMono(Font.BOLD, 20f);
    static final Font F_BIG    = pickMono(Font.BOLD, 27f);

    static void applyTheme(String name) {
        Theme t = findTheme(name);
        BG = t.bg; BG_PANEL = t.bgPanel; BG_CARD = t.bgCard; BG_FIELD = t.bgField;
        LINE = t.line; NEON_CYAN = t.neonCyan; NEON_PINK = t.neonPink;
        NEON_PURP = t.neonPurp; NEON_GRN = t.neonGrn; NEON_YEL = t.neonYel;
        TXT = t.txt; TXT_DIM = t.txtDim; BG_GRAD = t.bgGrad;
        DIM_1 = t.dim1; DIM_2 = t.dim2; SCROLL_C = t.scrollC;
        matrixRain = t.matrixRain;
    }

    static VaultManager manager;
    final CardLayoutScreens screens = new CardLayoutScreens();
    final JPanel screenHolder = new JPanel(screens.layout);

    JPanel authCard; javax.swing.border.Border authCardBorder;
    JLabel authTitle, authSub, authStatus, authLbl2;
    JPasswordField authPass, authPass2;
    JCheckBox authShow;
    CyberButton authBtn;

    java.awt.CardLayout contentCards = new java.awt.CardLayout();
    JPanel contentHolder;
    NavButton navPass, navTok, navGen;
    JLabel statsLabel;
    JTextField passSearch, tokSearch;
    JScrollPane passScroll, tokScroll;
    JPanel passTagBar, tokTagBar;
    boolean showFavPass = false, showFavTok = false;

    JTextField genOut; JSlider genLen; JLabel genLenVal, meterLabel;
    JCheckBox gUp, gLo, gDg, gSy, gAmb;
    StrengthMeter meter;

    Timer clipClear;
    Timer autoLockTimer;
    long lastActivity = System.currentTimeMillis();
    JLabel autoLockLabel;
    static final long AUTO_LOCK_MS = 5 * 60 * 1000L;
    String selectedVaultName;
    boolean creatingNewVault = false;
    JPanel vaultSelectorPanel;




    /* FRAME */
    CyberVault() {
        setTitle("CYBERVAULT");
        try {
            java.net.URL iconUrl = CyberVault.class.getResource("/assets/icon.png");
            if (iconUrl != null) setIconImage(Toolkit.getDefaultToolkit().getImage(iconUrl));
        } catch (Exception ignored) {
        }
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 700);
        setMinimumSize(new Dimension(940, 620));
        setLocationRelativeTo(null);
        try { manager = new VaultManager(); } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to initialize: " + e.getMessage());
            System.exit(1);
        }

//        JPanel root = new JPanel(new BorderLayout());
//        root.setBackground(BG);
//        root.setBorder(BorderFactory.createLineBorder(shade(NEON_CYAN, 0.55f)));
//        root.add(buildTitleBar(), BorderLayout.NORTH);
//        root.add(screenHolder, BorderLayout.CENTER);
//        setContentPane(root);
//
//        screenHolder.setBackground(BG);
//        vaultSelectorPanel = buildVaultSelector();
//        screenHolder.add(vaultSelectorPanel, "VAULTS");
//        screenHolder.add(buildAuthScreen(), "AUTH");
//        screenHolder.add(buildAppScreen(), "APP");
//        configureAuthMode();
//        screens.layout.show(screenHolder, "VAULTS");

        applyTheme(manager.theme);
        buildFrame();
        startActivityMonitor();
    }

    void buildFrame() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(shade(NEON_CYAN, 0.55f)));
        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(screenHolder, BorderLayout.CENTER);
        setContentPane(root);
        screenHolder.removeAll();
        screenHolder.setBackground(BG);
        vaultSelectorPanel = buildVaultSelector();
        screenHolder.add(vaultSelectorPanel, "VAULTS");
        screenHolder.add(buildAuthScreen(), "AUTH");
        screenHolder.add(buildAppScreen(), "APP");
        configureAuthMode();
        if (manager.active != null) {
            refreshPasswords(); refreshTokens(); updateStats();
            selectNav(navPass, "PASS");
            screens.layout.show(screenHolder, "APP");
        } else {
            screens.layout.show(screenHolder, "VAULTS");
        }
        revalidate();
        repaint();
    }

    void switchTheme() {
        String[] themes = {"cyberpunk", "matrix", "dark", "light"};
        int idx = 0;
        for (int i = 0; i < themes.length; i++) {
            if (themes[i].equals(manager.theme)) { idx = i; break; }
        }
        String next = themes[(idx + 1) % themes.length];
        try { manager.setTheme(next); } catch (Exception ex) {}
        applyTheme(next);
        buildFrame();
    }

    static class CardLayoutScreens { java.awt.CardLayout layout = new java.awt.CardLayout(); }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            CyberVault cv = new CyberVault();
            cv.setVisible(true);
        });
    }

    /* TITLE BAR */
    JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setPreferredSize(new Dimension(0, 36));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE));
        JLabel t = label("  CYBERVAULT // PERSONAL DATA TERMINAL", F_MONO_S, TXT_DIM);
        bar.add(t, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 6));
        btns.setOpaque(false);

        JButton settingsBtn = miniBtn("SETTINGS", ev -> openSettings(), NEON_CYAN);
        settingsBtn.setPreferredSize(new Dimension(96, 22));
        btns.add(settingsBtn);
        btns.add(miniBtn("\u2500", ev -> setState(JFrame.ICONIFIED), NEON_CYAN));
        btns.add(miniBtn("\u2715", ev -> System.exit(0), NEON_PINK));
        bar.add(btns, BorderLayout.EAST);
        MouseAdapter drag = windowDrag(this);
        bar.addMouseListener(drag); bar.addMouseMotionListener(drag);
        t.addMouseListener(drag);   t.addMouseMotionListener(drag);
        return bar;
    }

    JButton miniBtn(String txt, ActionListener al, Color hover) {
        JButton b = new JButton(txt);
        b.setFont(F_MONO_S);
        b.setForeground(TXT_DIM);
        b.setPreferredSize(new Dimension(28, 22));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(hover); }
            public void mouseExited(MouseEvent e)  { b.setForeground(TXT_DIM); }
        });
        b.addActionListener(al);
        return b;
    }

    static MouseAdapter windowDrag(Window w) {
        return new MouseAdapter() {
            Point start, winStart;
            public void mousePressed(MouseEvent e) { start = e.getLocationOnScreen(); winStart = w.getLocation(); }
            public void mouseDragged(MouseEvent e) {
                Point p = e.getLocationOnScreen();
                w.setLocation(winStart.x + p.x - start.x, winStart.y + p.y - start.y);
            }
        };
    }

    /* AUTH SCREEN */
    JPanel buildAuthScreen() {
        JPanel bg = new GridBG();
        bg.setLayout(new GridBagLayout());

        authCard = new JPanel(new BorderLayout());
        authCard.setBackground(BG_PANEL);
        authCardBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NEON_CYAN),
                        BorderFactory.createMatteBorder(0, 4, 0, 0, NEON_PINK)),
                empty(28, 36, 28, 36));
        authCard.setBorder(authCardBorder);
        authCard.setPreferredSize(new Dimension(440, 560));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1;

        g.gridy = 0; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.CENTER; g.insets = new Insets(0,0,0,0);
        form.add(new HexLogo(92), g);

        g.fill = GridBagConstraints.HORIZONTAL;
        JLabel brand = label("C Y B E R V A U L T", F_BIG, NEON_CYAN);
        brand.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy++; g.insets = new Insets(12, 0, 0, 0);
        form.add(brand, g);

        JLabel tag = label("// LOCAL  \u2022  AES-256  \u2022  ZERO CLOUD", F_MONO_S, TXT_DIM);
        tag.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy++; g.insets = new Insets(6, 0, 0, 0);
        form.add(tag, g);

        g.anchor = GridBagConstraints.WEST;
        authTitle = label("ACCESS VAULT", F_MONO_B, NEON_GRN);
        g.gridy++; g.insets = new Insets(26, 0, 3, 0);
        form.add(authTitle, g);
        authSub = label("// enter master key to decrypt", F_MONO_S, TXT_DIM);
        g.gridy++; g.insets = new Insets(0, 0, 14, 0);
        form.add(authSub, g);

        g.gridy++; g.insets = new Insets(4, 0, 4, 0);
        form.add(label("MASTER KEY", F_MONO_S, TXT_DIM), g);
        authPass = passField();
        g.gridy++; g.insets = new Insets(0, 0, 10, 0);
        form.add(authPass, g);

        authLbl2 = label("CONFIRM MASTER KEY", F_MONO_S, TXT_DIM);
        g.gridy++; g.insets = new Insets(0, 0, 4, 0);
        form.add(authLbl2, g);
        authPass2 = passField();
        g.gridy++; g.insets = new Insets(0, 0, 8, 0);
        form.add(authPass2, g);

        authShow = cyberCheck("SHOW MASTER KEY");
        authShow.addItemListener(ev -> {
            char ec = authShow.isSelected() ? (char) 0 : '\u2022';
            authPass.setEchoChar(ec); authPass2.setEchoChar(ec);
        });
        g.gridy++; g.insets = new Insets(2, 0, 16, 0);
        form.add(authShow, g);

        authBtn = new CyberButton("\u25B6 ACCESS VAULT", NEON_CYAN, true);
        authBtn.setPreferredSize(new Dimension(0, 46));
        authBtn.addActionListener(ev -> authAction());
        g.gridy++;
        form.add(authBtn, g);

        authStatus = label(" ", F_MONO_S, NEON_PINK);
        authStatus.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy++; g.insets = new Insets(12, 0, 0, 0);
        form.add(authStatus, g);

        authCard.add(form, BorderLayout.CENTER);
        authPass.addActionListener(ev -> authAction());
        authPass2.addActionListener(ev -> authAction());

        GridBagConstraints wrap = new GridBagConstraints();
        wrap.gridx = 0; wrap.gridy = 0;
        bg.add(authCard, wrap);
        return bg;
    }

    JPanel buildVaultSelector() {
        JPanel bg = new GridBG();
        bg.setLayout(new BorderLayout(0, 16));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = label("SELECT VAULT", F_TITLE, NEON_CYAN);
        title.setBorder(empty(24, 36, 0, 36));
        titlePanel.add(title, BorderLayout.NORTH);
        bg.add(titlePanel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(empty(0, 36, 0, 36));

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        if (manager.vaults.isEmpty()) {
            list.add(label("// no vaults yet — create your first one", F_MONO_S, TXT_DIM));
        }

        for (VaultInfo v : manager.vaults) {
            JPanel card = new JPanel(new BorderLayout(12, 0));
            card.setBackground(BG_PANEL);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_CYAN),
                empty(16, 20, 16, 20)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            JLabel name = label(v.name, F_MONO_B, TXT);
            card.add(name, BorderLayout.WEST);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btns.setOpaque(false);

            CyberButton open = new CyberButton("▶ OPEN", NEON_GRN, false);
            open.addActionListener(ev -> {
                selectedVaultName = v.name;
                configureAuthMode();
                screens.layout.show(screenHolder, "AUTH");
            });
            btns.add(open);

            CyberButton ren = new CyberButton("\u270E", NEON_CYAN, false);
            ren.addActionListener(ev -> {
                String nn = JOptionPane.showInputDialog(this, "New name for '" + v.name + "':", v.name);
                if (nn != null && !nn.trim().isEmpty() && !nn.trim().equals(v.name)) {
                    try { manager.renameVault(v.name, nn.trim()); refreshVaultSelector(); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
                }
            });
            btns.add(ren);

            if (true) {
                CyberButton del = new CyberButton("🗑", NEON_PINK, false);
                del.addActionListener(ev -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete vault '" + v.name + "'?\nThis cannot be undone.",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            manager.deleteVault(v.name);
                            screenHolder.remove(buildVaultSelector());
                            vaultSelectorPanel = buildVaultSelector();
        screenHolder.add(vaultSelectorPanel, "VAULTS");
                            screens.layout.show(screenHolder, "VAULTS");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage());
                        }
                    }
                });
                btns.add(del);
            }

            card.add(btns, BorderLayout.EAST);
            list.add(card);
            list.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        center.add(scroll, BorderLayout.CENTER);
        bg.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(empty(16, 36, 24, 36));

        CyberButton addBtn = new CyberButton("+ CREATE NEW VAULT", NEON_YEL, true);
        addBtn.addActionListener(ev -> showCreateVaultDialog());
        bottom.add(addBtn);

        CyberButton impBtn = new CyberButton("\u21E7 IMPORT", NEON_CYAN, true);
        impBtn.addActionListener(ev -> showImportVaultDialog());
        bottom.add(impBtn);
        bg.add(bottom, BorderLayout.SOUTH);

        return bg;
    }

    void showCreateVaultDialog() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel("Vault Name:"), BorderLayout.NORTH);
        JTextField nameField = new JTextField(20);
        panel.add(nameField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New Vault",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty");
                return;
            }
            selectedVaultName = name;
            creatingNewVault = true;
            configureAuthMode();
            screens.layout.show(screenHolder, "AUTH");
        }
    }

    void showImportVaultDialog() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(manager.dir.toFile());
        fc.setDialogTitle("Import vault file (.dat / .vault)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Vault files", "dat", "vault"));
        if (fc.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        java.io.File src = fc.getSelectedFile();
        String name = JOptionPane.showInputDialog(this, "Name for imported vault:", "Imported");
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim();
        try {
            String file = name.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".vault";
            for (VaultInfo v : manager.vaults) {
                if (v.file.equals(file)) throw new Exception("A vault with this file already exists");
            }
            Files.copy(src.toPath(), manager.dir.resolve(file));
            manager.vaults.add(new VaultInfo(name, file));
            manager.saveConfig();
            refreshVaultSelector();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage());
        }
    }

    void configureAuthMode() {
        boolean exists = vaultExists(selectedVaultName);
        authTitle.setText((exists ? "ACCESS VAULT: " : "CREATE VAULT: ") + selectedVaultName);
        authTitle.setForeground(exists ? NEON_GRN : NEON_YEL);
        authSub.setText(exists ? "// enter master key to decrypt"
            : "// create a master key for this vault (not recoverable)");
        authLbl2.setVisible(!exists); authPass2.setVisible(!exists);
        authBtn.setText(exists ? "\u25B6 ACCESS VAULT" : "\u25B6 CREATE VAULT");
        authStatus.setText(" ");
        authPass.setText(""); authPass2.setText("");
        authCard.revalidate(); authCard.repaint();
    }
    boolean vaultExists(String name) {
        for (VaultInfo v : manager.vaults) {
            if (v.name.equals(name)) {
                return Files.exists(manager.dir.resolve(v.file));
            }
        }
        return false;
    }

    void authAction() {
        char[] p1 = authPass.getPassword();
        char[] p2 = authPass2.getPassword();
        try {
            if (vaultExists(selectedVaultName)) {
                authStatus.setText("// decrypting\u2026");
                if (manager.openVault(selectedVaultName, p1) != null) enterApp();
                else { authStatus.setText("\u2715 ACCESS DENIED \u2014 WRONG MASTER KEY"); flashAuthError(); }
            } else {
                if (p1.length < 6) { authStatus.setText("\u2715 MASTER KEY TOO SHORT (MIN 6)"); return; }
                if (!Arrays.equals(p1, p2)) { authStatus.setText("\u2715 KEYS DO NOT MATCH"); return; }
                authStatus.setText("// creating vault\u2026");
                if (creatingNewVault) manager.createVault(selectedVaultName, p1);
                else manager.initExisting(selectedVaultName, p1);
                creatingNewVault = false;
                enterApp();
            }
        } catch (Exception ex) {
            authStatus.setText("\u2715 ERROR: " + ex.getMessage());
        } finally {
            Arrays.fill(p1, '\0'); Arrays.fill(p2, '\0');
        }
    }

    void flashAuthError() {
        authCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NEON_PINK),
                        BorderFactory.createMatteBorder(0, 4, 0, 0, NEON_PINK)),
                empty(28, 36, 28, 36)));
        Timer t = new Timer(650, ev -> authCard.setBorder(authCardBorder));
        t.setRepeats(false); t.start();
    }

    void enterApp() {
        authPass.setText(""); authPass2.setText("");
        creatingNewVault = false;
        refreshPasswords(); refreshTokens(); updateStats();
        selectNav(navPass, "PASS");
        screens.layout.show(screenHolder, "APP");
    }

    void refreshVaultSelector() {
        screenHolder.remove(vaultSelectorPanel);
        vaultSelectorPanel = buildVaultSelector();
        screenHolder.add(vaultSelectorPanel, "VAULTS");
        screens.layout.show(screenHolder, "VAULTS");
    }

    void lockVault() {
        if (manager.active != null) manager.active.lock();
        manager.active = null;
        refreshVaultSelector();
    }

    /* TRAY */

    /* AUTO-LOCK */
    void resetActivity() {
        lastActivity = System.currentTimeMillis();
    }

    void startActivityMonitor() {
        // Listener سراسری — همه eventهای ماوس/کیبورد رو می‌گیره
        Toolkit.getDefaultToolkit().addAWTEventListener(ev -> {
            int id = ev.getID();
            if (id == java.awt.event.MouseEvent.MOUSE_MOVED ||
                id == java.awt.event.MouseEvent.MOUSE_PRESSED ||
                id == java.awt.event.KeyEvent.KEY_PRESSED) {
                resetActivity();
            }
        }, java.awt.AWTEvent.MOUSE_EVENT_MASK |
            java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK |
            java.awt.AWTEvent.KEY_EVENT_MASK);

        // تایمر هر ثانیه چک می‌کنه
        autoLockTimer = new Timer(1000, ev -> {
            if (manager.active == null) return;
            long idle = System.currentTimeMillis() - lastActivity;
            if (idle >= AUTO_LOCK_MS) {
                lockVault();
                return;
            }
            // نمایش شمارش معکوس
            long remain = (AUTO_LOCK_MS - idle) / 1000;
            long m = remain / 60, sec = remain % 60;
            String timeStr = String.format("%d:%02d", m, sec);
            Color color = remain < 60 ? NEON_PINK : remain < 180 ? NEON_YEL : TXT_DIM;
            autoLockLabel.setText("// AUTO-LOCK IN " + timeStr);
            autoLockLabel.setForeground(color);
        });
        autoLockTimer.start();
    }


    /* MAIN APP */
    JPanel buildAppScreen() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(buildSidebar(), BorderLayout.WEST);
        contentHolder = new JPanel(contentCards);
        contentHolder.setBackground(BG);
        contentHolder.add(buildPasswordsPanel(), "PASS");
        contentHolder.add(buildTokensPanel(), "TOK");
        contentHolder.add(buildGeneratorPanel(), "GEN");
        p.add(contentHolder, BorderLayout.CENTER);
        return p;
    }

    JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBackground(BG_PANEL);
        sb.setPreferredSize(new Dimension(232, 0));
        sb.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, LINE));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setBorder(empty(18, 16, 18, 12));
        brand.add(new HexLogo(46), BorderLayout.WEST);
        JPanel txts = new JPanel(new GridLayout(0, 1, 0, 2));
        txts.setOpaque(false);
        txts.add(label("CYBERVAULT", F_MONO_B, NEON_CYAN));
        txts.add(label("v1.6.0 // SECURE", F_MONO_S, TXT_DIM));
        brand.add(txts, BorderLayout.CENTER);
        sb.add(brand, BorderLayout.NORTH);

        JPanel navWrap = new JPanel(new GridBagLayout());
        navWrap.setOpaque(false);
        navWrap.setBorder(empty(6, 10, 6, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.insets = new Insets(3, 0, 3, 0);

        navPass = new NavButton("> PASSWORDS", NEON_CYAN);
        navTok  = new NavButton("> API TOKENS", NEON_PURP);
        navGen  = new NavButton("> GENERATOR", NEON_YEL);
        navPass.addActionListener(ev -> selectNav(navPass, "PASS"));
        navTok.addActionListener(ev -> selectNav(navTok, "TOK"));
        navGen.addActionListener(ev -> selectNav(navGen, "GEN"));
        g.gridy = 0; navWrap.add(navPass, g);
        g.gridy = 1; navWrap.add(navTok, g);
        g.gridy = 2; navWrap.add(navGen, g);
        g.gridy = 3; g.weighty = 1;
        JPanel filler = new JPanel(); filler.setOpaque(false);
        navWrap.add(filler, g);
        sb.add(navWrap, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 12));
        south.setOpaque(false);
        south.setBorder(empty(12, 14, 16, 14));
        autoLockLabel = label("// AUTO-LOCK: --:--", F_MONO_S, TXT_DIM);
     statsLabel = label("\u2026", F_MONO_S, TXT_DIM);
        JLabel foot = label("LOCAL ONLY // NO CLOUD", F_MONO_S, new Color(0x454B6E));
        JPanel sp = new JPanel(new GridLayout(0, 1, 0, 5));
        sp.setOpaque(false);
        sp.add(statsLabel); sp.add(autoLockLabel); sp.add(foot);
        CyberButton lock = new CyberButton("// LOCK VAULT", NEON_PINK, false);
        lock.setPreferredSize(new Dimension(0, 40));
        lock.addActionListener(ev -> lockVault());
        south.add(sp, BorderLayout.CENTER);
        south.add(lock, BorderLayout.SOUTH);
        sb.add(south, BorderLayout.SOUTH);
        return sb;
    }

    void selectNav(NavButton b, String card) {
        navPass.setActive(b == navPass);
        navTok.setActive(b == navTok);
        navGen.setActive(b == navGen);
        contentCards.show(contentHolder, card);
    }

    void updateStats() {
        int p = manager.active.data == null ? 0 : manager.active.data.passwords.size();
        int t = manager.active.data == null ? 0 : manager.active.data.tokens.size();
        statsLabel.setText(p + " CREDS // " + t + " TOKENS");
    }

    /* PASSWORDS PANEL */
    JPanel buildPasswordsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(empty(22, 26, 20, 22));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(sectionHeader("PASSWORD DATABASE", "// logins \u2022 emails \u2022 accounts", NEON_CYAN), BorderLayout.CENTER);
        CyberButton add = new CyberButton("+ NEW ENTRY", NEON_CYAN, true);
        add.setPreferredSize(new Dimension(150, 38));
        add.addActionListener(ev -> openPasswordDialog(null));

        CyberButton favPass = new CyberButton(showFavPass ? "\u2605 FAVORITES" : "\u2606 FAVORITES", NEON_YEL, false);
        favPass.addActionListener(ev -> {
            showFavPass = !showFavPass;
            favPass.setText(showFavPass ? "\u2605 FAVORITES" : "\u2606 FAVORITES");
            refreshPasswords();
        });

        JPanel addWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addWrap.setOpaque(false); addWrap.add(favPass); addWrap.add(add);
        head.add(addWrap, BorderLayout.EAST);

        passSearch = searchField("SEARCH ENTRIES\u2026");
        passSearch.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { refreshPasswords(); } });

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(passSearch, BorderLayout.CENTER);
        passTagBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        passTagBar.setOpaque(false);
        JPanel northWrap = new JPanel(new BorderLayout(0, 8));
        northWrap.setOpaque(false);
        northWrap.add(top, BorderLayout.NORTH);
        northWrap.add(passTagBar, BorderLayout.CENTER);
        p.add(northWrap, BorderLayout.NORTH);
        JPanel placeholder = new JPanel(new BorderLayout()); placeholder.setOpaque(false);
        passScroll = cyberScroll(placeholder);
        p.add(passScroll, BorderLayout.CENTER);
        return p;
    }

    void refreshPasswords() {
        String q = queryOf(passSearch).toLowerCase();
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(BG);
        inner.setBorder(empty(4, 2, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        int row = 0;
        if (manager.active.data != null) {
            for (PasswordEntry e : manager.active.data.passwords) {
                if (showFavPass && !e.favorite) continue;
                if (!q.isEmpty() && !((e.title + " " + e.username + " " + e.url + " " + tagsStr(e.tags)).toLowerCase().contains(q))) continue;
                g.gridy = row++; g.insets = new Insets(0, 0, 12, 0);
                inner.add(buildPasswordCard(e), g);
            }
        }
        if (row == 0) {
            g.gridy = 0; g.insets = new Insets(30, 0, 0, 0);
            inner.add(emptyState(q.isEmpty() ? "NO RECORDS YET // CLICK [+ NEW ENTRY]"
                    : "NO MATCH FOUND"), g);
        }
        g.gridy = row; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        JPanel fill = new JPanel(); fill.setOpaque(false);
        inner.add(fill, g);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setBackground(BG);
        wrap.add(inner, BorderLayout.CENTER);
        passScroll.getViewport().setView(wrap);
        passScroll.getViewport().setBackground(BG);
        passTagBar.removeAll();
        if (manager.active.data != null) {
            java.util.LinkedHashSet<String> allTags = new java.util.LinkedHashSet<>();
            for (PasswordEntry e2 : manager.active.data.passwords)
                if (e2.tags != null) allTags.addAll(e2.tags);
            for (String tg : allTags) {
                JButton tb = chip("#" + tg, NEON_PURP);
                tb.addActionListener(ev -> { passSearch.setText(tg); passSearch.setForeground(TXT); refreshPasswords(); });
                passTagBar.add(tb);
            }
        }
        passTagBar.revalidate(); passTagBar.repaint();
        passScroll.revalidate();
        updateStats();
    }

    JPanel buildPasswordCard(PasswordEntry e) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LINE),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, NEON_CYAN)),
                empty(14, 16, 12, 14)));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel ttl = new JPanel(new GridLayout(0, 1, 0, 2));
        ttl.setOpaque(false);
        ttl.add(label(e.title.toUpperCase(), pickMono(Font.BOLD, 14f), TXT));
        ttl.add(label("ADDED " + fmtDate(e.created), F_MONO_S, new Color(0x555C82)));
        head.add(ttl, BorderLayout.CENTER);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        JButton fav = chip(e.favorite ? "\u2605" : "\u2606", NEON_YEL);
        fav.addActionListener(ev -> { e.favorite = !e.favorite; saveVault(); refreshPasswords(); });
        JButton edit = chip("EDIT", NEON_CYAN);
        JButton del = chip("DEL", NEON_PINK);
        edit.addActionListener(ev -> openPasswordDialog(e));
        del.addActionListener(ev -> {
            if (confirmAction("DELETE \"" + e.title.toUpperCase() + "\" PERMANENTLY?")) {
                manager.active.data.passwords.remove(e);
                saveVault(); refreshPasswords();
            }
        });

        acts.add(fav); acts.add(edit); acts.add(del);
        head.add(acts, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(0, 1, 0, 7));
        body.setOpaque(false);
        body.setBorder(empty(12, 0, 10, 0));

        body.add(row("USER/MAIL", label(cut(e.username, 60), F_MONO, TXT), actsOf(copyChip(e.username))));

        JLabel pv = label(mask(e.password.length()), F_MONO, NEON_PINK);
        JButton show = chip("SHOW", NEON_YEL);
        JButton copyPw = copyChip(e.password);
        boolean[] vis = { false };
        show.addActionListener(ev -> {
            vis[0] = !vis[0];
            pv.setText(vis[0] ? cut(e.password, 60) : mask(e.password.length()));
            pv.setForeground(vis[0] ? NEON_GRN : NEON_PINK);
            show.setText(vis[0] ? "HIDE" : "SHOW");
        });
        body.add(row("PASSWORD", pv, actsOf(show, copyPw)));

        if (!e.url.isEmpty()) {
            JButton open = chip("OPEN", NEON_CYAN);
            open.addActionListener(ev -> openUrl(e.url));
            body.add(row("URL", label(cut(e.url, 60), F_MONO, NEON_CYAN), actsOf(open, copyChip(e.url))));
        }
        if (!e.notes.isEmpty())
            body.add(row("NOTES", label(cut(e.notes, 70), F_MONO, TXT_DIM), null));

        if (e.tags != null && !e.tags.isEmpty()) {
            JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            tagPanel.setOpaque(false);
            for (String tg : e.tags) {
                JButton tc = chip("#" + tg, NEON_PURP);
                tc.addActionListener(ev -> { passSearch.setText(tg); passSearch.setForeground(TXT); refreshPasswords(); });
                tagPanel.add(tc);
            }
            body.add(tagPanel);
        }

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    void openPasswordDialog(PasswordEntry ex) {
        JDialog d = cyberDialog(ex == null ? "NEW PASSWORD ENTRY" : "EDIT ENTRY", NEON_CYAN);
        JTextField fTitle = field(), fUser = field(), fUrl = field();
        JPasswordField fPass = passField();
        JTextArea fNotes = area();
        JTextField fTags = field();

        if (ex != null) {
            fTitle.setText(ex.title); fUser.setText(ex.username);
            fPass.setText(ex.password); fUrl.setText(ex.url); fNotes.setText(ex.notes);
            fTags.setText(ex.tags == null ? "" : String.join(", ", ex.tags));
        }
        JLabel err = label(" ", F_MONO_S, NEON_PINK);

        JButton gen = chip("\u26A1 GEN", NEON_YEL);
        gen.addActionListener(ev -> { fPass.setText(genPassword(18, true, true, true, true, false)); fPass.setEchoChar((char) 0); });
        JButton eye = chip("SHOW", NEON_PURP);
        boolean[] vis = { false };
        eye.addActionListener(ev -> { vis[0] = !vis[0]; fPass.setEchoChar(vis[0] ? (char) 0 : '\u2022'); eye.setText(vis[0] ? "HIDE" : "SHOW"); });
        JPanel passRow = new JPanel(new BorderLayout(8, 0));
        passRow.setBackground(BG_PANEL);
        JPanel passBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        passBtns.setOpaque(false); passBtns.add(gen); passBtns.add(eye);
        passRow.add(fPass, BorderLayout.CENTER);
        passRow.add(passBtns, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addFormRow(form, 0, "TITLE *", fTitle);
        addFormRow(form, 1, "USER / EMAIL *", fUser);
        addFormRow(form, 2, "PASSWORD *", passRow);
        addFormRow(form, 3, "URL", fUrl);
        JScrollPane ns = new JScrollPane(fNotes);
        ns.setPreferredSize(new Dimension(0, 70));
        ns.setBorder(BorderFactory.createLineBorder(LINE));
        styleScroll(ns); ns.getViewport().setBackground(BG_FIELD);
        addFormRow(form, 4, "TAGS", fTags);
        addFormRow(form, 5, "NOTES", ns);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(22, 24, 24, 24));
        body.add(form, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout(0, 10));
        foot.setOpaque(false);
        foot.add(err, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        CyberButton save = new CyberButton("SAVE ENTRY", NEON_CYAN, true);
        CyberButton cancel = new CyberButton("CANCEL", TXT_DIM, false);
        save.addActionListener(ev -> {
            String t = fTitle.getText().trim(), u = fUser.getText().trim(), pw = new String(fPass.getPassword());
            if (t.isEmpty() || u.isEmpty() || pw.isEmpty()) { err.setText("\u2715 TITLE, USER & PASSWORD REQUIRED"); return; }
            PasswordEntry ent = ex != null ? ex : new PasswordEntry();
            ent.title = t; ent.username = u; ent.password = pw;
            ent.url = fUrl.getText().trim(); ent.notes = fNotes.getText().trim();
            ent.tags = parseTags(fTags.getText());
            if (ex == null) manager.active.data.passwords.add(ent);
            if (saveVault()) { refreshPasswords(); d.dispose(); }
        });
        cancel.addActionListener(ev -> d.dispose());
        btns.add(save); btns.add(cancel);
        foot.add(btns, BorderLayout.SOUTH);
        body.add(foot, BorderLayout.SOUTH);

        d.add(body, BorderLayout.CENTER);
        d.setSize(560, 500);
        d.setLocationRelativeTo(this);
        escapeToClose(d);
        d.setVisible(true);
    }



    /* TOKENS PANEL */
    JPanel buildTokensPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(empty(22, 26, 20, 22));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(sectionHeader("API TOKENS", "// hugging face \u2022 openai \u2022 github \u2022 \u2026", NEON_PURP), BorderLayout.CENTER);
        CyberButton add = new CyberButton("+ NEW TOKEN", NEON_PURP, true);
        add.setPreferredSize(new Dimension(160, 38));
        add.addActionListener(ev -> openTokenDialog(null));

        CyberButton favTok = new CyberButton(showFavTok ? "\u2605 FAVORITES" : "\u2606 FAVORITES", NEON_YEL, false);
        favTok.addActionListener(ev -> {
            showFavTok = !showFavTok;
            favTok.setText(showFavTok ? "\u2605 FAVORITES" : "\u2606 FAVORITES");
            refreshTokens();
        });
        JPanel addWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addWrap.setOpaque(false); addWrap.add(favTok); addWrap.add(add);
        head.add(addWrap, BorderLayout.EAST);

        tokSearch = searchField("SEARCH TOKENS\u2026");
        tokSearch.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { refreshTokens(); } });

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(tokSearch, BorderLayout.CENTER);
        tokTagBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tokTagBar.setOpaque(false);
        JPanel northWrap = new JPanel(new BorderLayout(0, 8));
        northWrap.setOpaque(false);
        northWrap.add(top, BorderLayout.NORTH);
        northWrap.add(tokTagBar, BorderLayout.CENTER);
        p.add(northWrap, BorderLayout.NORTH);

        JPanel placeholder = new JPanel(new BorderLayout()); placeholder.setOpaque(false);
        tokScroll = cyberScroll(placeholder);
        p.add(tokScroll, BorderLayout.CENTER);
        return p;
    }

    void refreshTokens() {
        String q = queryOf(tokSearch).toLowerCase();
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(BG);
        inner.setBorder(empty(4, 2, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        int row = 0;
        if (manager.active.data != null) {
            for (TokenEntry e : manager.active.data.tokens) {
                if (showFavTok && !e.favorite) continue;
                if (!q.isEmpty() && !((e.name + " " + e.notes + " " + tagsStr(e.tags)).toLowerCase().contains(q))) continue;
                g.gridy = row++; g.insets = new Insets(0, 0, 12, 0);
                inner.add(buildTokenCard(e), g);
            }
        }
        if (row == 0) {
            g.gridy = 0; g.insets = new Insets(30, 0, 0, 0);
            inner.add(emptyState(q.isEmpty() ? "NO TOKENS YET // CLICK [+ NEW TOKEN]"
                    : "NO MATCH FOUND"), g);
        }
        g.gridy = row; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        JPanel fill = new JPanel(); fill.setOpaque(false);
        inner.add(fill, g);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setBackground(BG);
        wrap.add(inner, BorderLayout.CENTER);
        tokScroll.getViewport().setView(wrap);
        tokScroll.getViewport().setBackground(BG);
        tokTagBar.removeAll();
        if (manager.active.data != null) {
            java.util.LinkedHashSet<String> allTags = new java.util.LinkedHashSet<>();
            for (TokenEntry e2 : manager.active.data.tokens)
                if (e2.tags != null) allTags.addAll(e2.tags);
            for (String tg : allTags) {
                JButton tb = chip("#" + tg, NEON_PURP);
                tb.addActionListener(ev -> { tokSearch.setText(tg); tokSearch.setForeground(TXT); refreshTokens(); });
                tokTagBar.add(tb);
            }
        }
        tokTagBar.revalidate(); tokTagBar.repaint();
        tokScroll.revalidate();
        updateStats();
    }

    JPanel buildTokenCard(TokenEntry e) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LINE),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, NEON_PURP)),
                empty(14, 16, 12, 14)));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel ttl = new JPanel(new GridLayout(0, 1, 0, 2));
        ttl.setOpaque(false);
        ttl.add(label(e.name.toUpperCase(), pickMono(Font.BOLD, 14f), NEON_PURP));
        ttl.add(label("ADDED " + fmtDate(e.created), F_MONO_S, new Color(0x555C82)));
        head.add(ttl, BorderLayout.CENTER);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        JButton fav = chip(e.favorite ? "\u2605" : "\u2606", NEON_YEL);
        fav.addActionListener(ev -> { e.favorite = !e.favorite; saveVault(); refreshTokens(); });
        JButton edit = chip("EDIT", NEON_CYAN);
        JButton del = chip("DEL", NEON_PINK);
        edit.addActionListener(ev -> openTokenDialog(e));
        del.addActionListener(ev -> {
            if (confirmAction("DELETE TOKEN \"" + e.name.toUpperCase() + "\" PERMANENTLY?")) {
                manager.active.data.tokens.remove(e);
                saveVault(); refreshTokens();
            }
        });

        acts.add(fav); acts.add(edit); acts.add(del);
        head.add(acts, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(0, 1, 0, 7));
        body.setOpaque(false);
        body.setBorder(empty(12, 0, 10, 0));

        JLabel tv = label(mask(e.token.length()), F_MONO, NEON_PINK);
        JButton show = chip("SHOW", NEON_YEL);
        boolean[] vis = { false };
        show.addActionListener(ev -> {
            vis[0] = !vis[0];
            tv.setText(vis[0] ? cut(e.token, 60) : mask(e.token.length()));
            tv.setForeground(vis[0] ? NEON_GRN : NEON_PINK);
            show.setText(vis[0] ? "HIDE" : "SHOW");
        });
        body.add(row("TOKEN", tv, actsOf(show, copyChip(e.token))));
        if (!e.notes.isEmpty())
            body.add(row("NOTES", label(cut(e.notes, 70), F_MONO, TXT_DIM), null));

        if (e.tags != null && !e.tags.isEmpty()) {
            JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            tagPanel.setOpaque(false);
            for (String tg : e.tags) {
                JButton tc = chip("#" + tg, NEON_PURP);
                tc.addActionListener(ev -> { tokSearch.setText(tg); tokSearch.setForeground(TXT); refreshTokens(); });
                tagPanel.add(tc);
            }
            body.add(tagPanel);
        }

        card.add(body, BorderLayout.CENTER);
        return card;
    }


    void openTokenDialog(TokenEntry ex) {
        JDialog d = cyberDialog(ex == null ? "NEW API TOKEN" : "EDIT API TOKEN", NEON_PURP);
        JTextField fName = field();
        JPasswordField fTok = passField();
        JTextArea fNotes = area();
        JTextField fTags = field();
        if (ex != null) { fName.setText(ex.name); fTok.setText(ex.token); fNotes.setText(ex.notes);
            fTags.setText(ex.tags == null ? "" : String.join(", ", ex.tags)); }
        JLabel err = label(" ", F_MONO_S, NEON_PINK);

        JButton show = chip("SHOW", NEON_YEL);
        boolean[] vis = { false };
        show.addActionListener(ev -> { vis[0] = !vis[0]; fTok.setEchoChar(vis[0] ? (char) 0 : '\u2022'); show.setText(vis[0] ? "HIDE" : "SHOW"); });
        JPanel tokRow = new JPanel(new BorderLayout(8, 0));
        tokRow.setBackground(BG_PANEL);
        tokRow.add(fTok, BorderLayout.CENTER);
        tokRow.add(show, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addFormRow(form, 0, "PROVIDER *", fName);
        addFormRow(form, 1, "TOKEN *", tokRow);
        JScrollPane ns = new JScrollPane(fNotes);
        ns.setPreferredSize(new Dimension(0, 74));
        ns.setBorder(BorderFactory.createLineBorder(LINE));
        styleScroll(ns); ns.getViewport().setBackground(BG_FIELD);
        addFormRow(form, 2, "TAGS", fTags);
        addFormRow(form, 3, "NOTES", ns);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(22, 24, 24, 24));
        body.add(form, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout(0, 10));
        foot.setOpaque(false);
        foot.add(err, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        CyberButton save = new CyberButton("SAVE TOKEN", NEON_PURP, true);
        CyberButton cancel = new CyberButton("CANCEL", TXT_DIM, false);
        save.addActionListener(ev -> {
            String n = fName.getText().trim();
            String t = new String(fTok.getPassword()).trim();
            if (n.isEmpty() || t.isEmpty()) { err.setText("\u2715 PROVIDER & TOKEN ARE REQUIRED"); return; }
            TokenEntry ent = ex != null ? ex : new TokenEntry();
            ent.name = n; ent.token = t; ent.notes = fNotes.getText().trim();
            ent.tags = parseTags(fTags.getText());
            if (ex == null) manager.active.data.tokens.add(ent);
            if (saveVault()) { refreshTokens(); d.dispose(); }
        });
        cancel.addActionListener(ev -> d.dispose());
        btns.add(save); btns.add(cancel);
        foot.add(btns, BorderLayout.SOUTH);
        body.add(foot, BorderLayout.SOUTH);

        d.add(body, BorderLayout.CENTER);
        d.setSize(520, 420);
        d.setLocationRelativeTo(this);
        escapeToClose(d);
        d.setVisible(true);
    }

    /* GENERATOR */
    JPanel buildGeneratorPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);
        outer.setBorder(empty(22, 26, 20, 22));

        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), empty(24, 26, 26, 26)));
        card.add(sectionHeader("PASSWORD GENERATOR", "// forge fresh high-entropy keys", NEON_YEL), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;

        genOut = field();
        genOut.setFont(pickMono(Font.BOLD, 17f));
        g.gridy = 0; body.add(genOut, g);

        meter = new StrengthMeter();
        g.gridy = 1; g.insets = new Insets(14, 0, 4, 0);
        body.add(meter, g);
        meterLabel = label(" ", F_MONO_S, TXT_DIM);
        g.gridy = 2; g.insets = new Insets(0, 0, 18, 0);
        body.add(meterLabel, g);

        JPanel lenRow = new JPanel(new BorderLayout(14, 0));
        lenRow.setOpaque(false);
        genLenVal = label("18", F_MONO_B, NEON_YEL);
        genLenVal.setPreferredSize(new Dimension(34, 18));
        lenRow.add(label("LENGTH", F_MONO_S, TXT_DIM), BorderLayout.WEST);
        lenRow.add(genLenVal, BorderLayout.EAST);
        genLen = new JSlider(8, 64, 18);
        genLen.setOpaque(false);
        genLen.setFocusable(false);
        genLen.setUI(new BasicSliderUI(genLen) {
            public void paintTrack(Graphics gr) {
                Graphics2D g2 = (Graphics2D) gr.create();
                int y = trackRect.y + trackRect.height / 2 - 2;
                g2.setColor(LINE);
                g2.fillRect(trackRect.x, y, trackRect.width, 4);
                g2.setColor(withAlpha(NEON_YEL, 150));
                g2.fillRect(trackRect.x, y, Math.max(0, thumbRect.x - trackRect.x), 4);
                g2.dispose();
            }
            public void paintThumb(Graphics gr) {
                Graphics2D g2 = (Graphics2D) gr.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NEON_YEL);
                g2.fillRect(thumbRect.x + 3, thumbRect.y + 2, thumbRect.width - 6, thumbRect.height - 4);
                g2.dispose();
            }
            public void paintFocus(Graphics gr) {}
        });
        lenRow.add(genLen, BorderLayout.CENTER);
        g.gridy = 3; body.add(lenRow, g);

        gUp = cyberCheck("UPPERCASE  A-Z");
        gLo = cyberCheck("LOWERCASE  a-z");
        gDg = cyberCheck("NUMBERS  0-9");
        gSy = cyberCheck("SYMBOLS  !@#$%");
        gAmb = cyberCheck("NO AMBIGUOUS  O0lI1");
        gUp.setSelected(true); gLo.setSelected(true); gDg.setSelected(true); gSy.setSelected(true);
        JPanel opts = new JPanel(new GridLayout(0, 2, 10, 8));
        opts.setOpaque(false);
        opts.add(gUp); opts.add(gLo); opts.add(gDg); opts.add(gSy); opts.add(gAmb);
        g.gridy = 4; g.insets = new Insets(18, 0, 22, 0);
        body.add(opts, g);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        CyberButton genBtn = new CyberButton("\u26A1 GENERATE", NEON_YEL, true);
        genBtn.setPreferredSize(new Dimension(220, 44));
        CyberButton copyBtn = new CyberButton("COPY", NEON_GRN, false);
        copyBtn.setPreferredSize(new Dimension(120, 44));
        genBtn.addActionListener(ev -> regen());
        copyBtn.addActionListener(ev -> copyText(genOut.getText(), copyBtn));
        btnRow.add(genBtn); btnRow.add(copyBtn);
        g.gridy = 5;
        body.add(btnRow, g);

        card.add(body, BorderLayout.CENTER);

        genLen.addChangeListener(ev -> regen());
        ItemListener il = ev -> regen();
        gUp.addItemListener(il); gLo.addItemListener(il); gDg.addItemListener(il);
        gSy.addItemListener(il); gAmb.addItemListener(il);

        GridBagConstraints cg = new GridBagConstraints();
        cg.gridx = 0; cg.gridy = 0; cg.weightx = 1; cg.weighty = 1;
        cg.fill = GridBagConstraints.HORIZONTAL; cg.anchor = GridBagConstraints.NORTH;
        outer.add(card, cg);

        regen();
        return outer;
    }

    void regen() {
        int len = genLen.getValue();
        boolean up = gUp.isSelected(), lo = gLo.isSelected(), dg = gDg.isSelected(),
                sy = gSy.isSelected(), amb = gAmb.isSelected();
        genOut.setText(genPassword(len, up, lo, dg, sy, amb));
        int pool = poolSize(up, lo, dg, sy, amb);
        double bits = len * (Math.log(pool) / Math.log(2));
        meter.setBits(bits);
        meterLabel.setText("ENTROPY: " + (int) bits + " BITS  //  " + strengthWord(bits));
        genLenVal.setText(String.valueOf(len));
    }

    static String genPassword(int len, boolean up, boolean lo, boolean dg, boolean sy, boolean noAmb) {
        String U = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String L = "abcdefghijklmnopqrstuvwxyz";
        String D = "0123456789";
        String S = "!@#$%^&*()-_=+[]{};:,.?/<>~";
        if (noAmb) {
            U = U.replace("O", "").replace("I", "");
            L = L.replace("l", "");
            D = D.replace("0", "").replace("1", "");
        }
        List<String> pools = new ArrayList<>();
        if (up) pools.add(U);
        if (lo) pools.add(L);
        if (dg) pools.add(D);
        if (sy) pools.add(S);
        if (pools.isEmpty()) pools.add(L);
        StringBuilder all = new StringBuilder();
        for (String p : pools) all.append(p);
        SecureRandom r = new SecureRandom();
        char[] out = new char[len];
        int i = 0;
        for (String p : pools) if (i < len) out[i++] = p.charAt(r.nextInt(p.length()));
        while (i < len) out[i++] = all.charAt(r.nextInt(all.length()));
        for (int k = len - 1; k > 0; k--) {
            int j = r.nextInt(k + 1);
            char t = out[k]; out[k] = out[j]; out[j] = t;
        }
        return new String(out);
    }

    static int poolSize(boolean up, boolean lo, boolean dg, boolean sy, boolean amb) {
        int n = 0;
        if (up) n += amb ? 24 : 26;
        if (lo) n += amb ? 25 : 26;
        if (dg) n += amb ? 8 : 10;
        if (sy) n += 27;
        return n == 0 ? 26 : n;
    }

    static String strengthWord(double bits) {
        if (bits < 45) return "WEAK";
        if (bits < 70) return "FAIR";
        if (bits < 100) return "STRONG";
        return "ELITE";
    }


    /* DIALOGS & ACTIONS */
    JDialog cyberDialog(String title, Color accent) {
        JDialog d = new JDialog(this, true);
        d.setUndecorated(true);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PANEL);
        root.setBorder(BorderFactory.createLineBorder(accent));
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG);
        bar.setPreferredSize(new Dimension(0, 32));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE));
        JLabel t = label("  // " + title, F_MONO_S, accent);
        bar.add(t, BorderLayout.WEST);
        JButton x = miniBtn("\u2715", ev -> d.dispose(), NEON_PINK);
        JPanel xr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 3));
        xr.setOpaque(false); xr.add(x);
        bar.add(xr, BorderLayout.EAST);
        root.add(bar, BorderLayout.NORTH);
        MouseAdapter drag = windowDrag(d);
        bar.addMouseListener(drag); bar.addMouseMotionListener(drag);
        t.addMouseListener(drag);   t.addMouseMotionListener(drag);
        d.setContentPane(root);
        return d;
    }

    boolean confirmAction(String msg) {
        JDialog d = cyberDialog("CONFIRM ACTION", NEON_PINK);
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(24, 26, 24, 26));
        body.add(label("<html>" + msg + "</html>", F_MONO, TXT), BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        boolean[] r = { false };
        CyberButton yes = new CyberButton("CONFIRM", NEON_PINK, true);
        CyberButton no = new CyberButton("CANCEL", NEON_CYAN, false);
        yes.addActionListener(ev -> { r[0] = true; d.dispose(); });
        no.addActionListener(ev -> d.dispose());
        btns.add(yes); btns.add(no);
        body.add(btns, BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.setSize(440, 150);
        d.setLocationRelativeTo(this);
        escapeToClose(d);
        d.setVisible(true);
        return r[0];
    }

    void alert(String msg) {
        JDialog d = cyberDialog("SYSTEM ALERT", NEON_PINK);
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(24, 26, 24, 26));
        body.add(label(msg, F_MONO, TXT), BorderLayout.CENTER);
        CyberButton ok = new CyberButton("OK", NEON_CYAN, true);
        ok.addActionListener(ev -> d.dispose());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btns.setOpaque(false); btns.add(ok);
        body.add(btns, BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.setSize(420, 150);
        d.setLocationRelativeTo(this);
        escapeToClose(d);
        d.setVisible(true);
    }

    void openSettings() {
        JDialog d = cyberDialog("SETTINGS", NEON_CYAN);
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(24, 26, 24, 26));

        body.add(label("// SELECT THEME", F_MONO_B, TXT_DIM), BorderLayout.NORTH);

        JPanel grid = new ScrollGrid(new GridLayout(0, 2, 16, 16));

        for (Theme t : PRESETS) {
            JPanel card = new JPanel(new BorderLayout(0, 10));
            card.setBackground(BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.name.equals(manager.theme) ? NEON_CYAN : LINE, 2),
                empty(12, 14, 12, 14)));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // ── preview ──
            JPanel preview = new JPanel(new BorderLayout(0, 6));
            preview.setBackground(t.bg);
            preview.setBorder(BorderFactory.createLineBorder(t.line));
            preview.setPreferredSize(new Dimension(220, 150));

            JPanel pTitle = new JPanel(new BorderLayout());
            pTitle.setBackground(t.bgPanel);
            pTitle.setPreferredSize(new Dimension(0, 18));
            JLabel pLbl = new JLabel("  // " + t.displayName);
            pLbl.setFont(F_MONO_S); pLbl.setForeground(t.txtDim);
            pTitle.add(pLbl, BorderLayout.WEST);
            preview.add(pTitle, BorderLayout.NORTH);

            JPanel pCard = new JPanel(new BorderLayout(6, 4));
            pCard.setBackground(t.bgCard);
            pCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.line), empty(6, 8, 6, 8)));
            pCard.add(label("TITLE", pickMono(Font.BOLD, 10f), t.txt), BorderLayout.NORTH);
            pCard.add(label("••••••••", F_MONO_S, t.neonPink), BorderLayout.CENTER);
            JPanel pActs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            pActs.setOpaque(false);
            pActs.add(miniChip("EDIT", t.neonCyan, t.bgField));
            pActs.add(miniChip("\u2605", t.neonYel, t.bgField));
            pCard.add(pActs, BorderLayout.SOUTH);

            JPanel pWrap = new JPanel(new BorderLayout());
            pWrap.setOpaque(false); pWrap.setBorder(empty(6, 8, 6, 8));
            pWrap.add(pCard, BorderLayout.CENTER);
            preview.add(pWrap, BorderLayout.CENTER);

            // ── swatches ──
            JPanel swatches = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            swatches.setOpaque(false);
            Color[] palette = { t.neonCyan, t.neonPink, t.neonPurp, t.neonGrn, t.neonYel, t.txt };
            for (Color c : palette) {
                JPanel sw = new JPanel();
                sw.setBackground(c);
                sw.setPreferredSize(new Dimension(18, 18));
                sw.setBorder(BorderFactory.createLineBorder(LINE));
                swatches.add(sw);
            }
            preview.add(swatches, BorderLayout.SOUTH);

            card.add(preview, BorderLayout.CENTER);

            JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            nameRow.setOpaque(false);
            nameRow.add(label(t.displayName.toUpperCase(), pickMono(Font.BOLD, 13f), TXT));
            if (t.name.equals(manager.theme)) {
                nameRow.add(label("   [ACTIVE]", F_MONO_S, NEON_GRN));
            }
            card.add(nameRow, BorderLayout.SOUTH);

            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    try { manager.setTheme(t.name); } catch (Exception ex) {}
                    applyTheme(t.name);
                    buildFrame();
                    d.dispose();
                    openSettings();  // reopen to reflect new active
                }
                public void mouseEntered(MouseEvent e) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NEON_CYAN, 2), empty(12, 14, 12, 14)));
                }
                public void mouseExited(MouseEvent e) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(t.name.equals(manager.theme) ? NEON_CYAN : LINE, 2),
                        empty(12, 14, 12, 14)));
                }
            });
            grid.add(card);
        }

        JScrollPane scroll = cyberScroll(grid);
        scroll.getViewport().setBackground(BG_PANEL);
        body.add(scroll, BorderLayout.CENTER);
        d.add(body, BorderLayout.CENTER);
        d.setSize(600, 580);
        d.setLocationRelativeTo(this);
        escapeToClose(d);
        d.setVisible(true);
    }

    static JComponent miniChip(String text, Color c, Color bg) {
        JComponent p = new JComponent() {
            public Dimension getPreferredSize() { return new Dimension(46, 16); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(bg);
                g2.fillRect(0, 0, w, h);
                g2.setColor(c);
                g2.drawRect(0, 0, w - 1, h - 1);
                g2.setFont(pickMono(Font.BOLD, 8f));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                g2.drawString(text, (w - tw) / 2, (h + fm.getAscent() - 2) / 2);
                g2.dispose();
            }
        };
        return p;
    }

    boolean saveVault() {
        try { manager.active.save(); return true; }
        catch (Exception ex) { alert("VAULT SAVE FAILED: " + ex.getMessage()); return false; }
    }

    void copyText(String s, JButton src) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
        if (clipClear != null) clipClear.stop();
        clipClear = new Timer(20000, ev -> {
            try {
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    String cur = (String) cb.getData(DataFlavor.stringFlavor);
                    if (s.equals(cur)) cb.setContents(new StringSelection(""), null);
                }
            } catch (Exception ignored) {}
        });
        clipClear.setRepeats(false);
        clipClear.start();
        String old = src.getText();
        src.setText("\u2713 COPIED");
        Timer back = new Timer(1100, ev -> src.setText(old));
        back.setRepeats(false); back.start();
    }

    JButton copyChip(String secret) {
        JButton b = chip("COPY", NEON_GRN);
        b.addActionListener(ev -> copyText(secret, b));
        return b;
    }

    void openUrl(String url) {
        try {
            String u = url.startsWith("http") ? url : "https://" + url;
            Desktop.getDesktop().browse(new URI(u));
        } catch (Exception ignored) {}
    }

    static void escapeToClose(JDialog d) {
        d.getRootPane().registerKeyboardAction(ev -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }



    /* UI HELPERS */
    static JLabel label(String s, Font f, Color c) {
        JLabel l = new JLabel(s); l.setFont(f); l.setForeground(c); return l;
    }

    static javax.swing.border.Border empty(int t, int l, int b, int r) {
        return BorderFactory.createEmptyBorder(t, l, b, r);
    }
    static javax.swing.border.Border empty(int v) {
        return BorderFactory.createEmptyBorder(v, v, v, v);
    }

    static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
    static Color shade(Color c, float f) {
        return new Color((int) (c.getRed() * f), (int) (c.getGreen() * f), (int) (c.getBlue() * f));
    }

    static void styleField(JTextField tf) {
        tf.setBackground(BG_FIELD);
        tf.setForeground(TXT);
        tf.setCaretColor(NEON_CYAN);
        tf.setFont(F_MONO);
        javax.swing.border.Border pad = BorderFactory.createEmptyBorder(9, 12, 9, 12);
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), pad));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(NEON_CYAN), pad));
            }
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), pad));
            }
        });
    }

    static JTextField field() { JTextField tf = new JTextField(); styleField(tf); return tf; }
    static JPasswordField passField() { JPasswordField pf = new JPasswordField(); styleField(pf); return pf; }

    static JTextArea area() {
        JTextArea ta = new JTextArea();
        ta.setBackground(BG_FIELD); ta.setForeground(TXT); ta.setCaretColor(NEON_CYAN);
        ta.setFont(F_MONO); ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setBorder(empty(8, 10, 8, 10));
        return ta;
    }

    static JTextField searchField(String hint) {
        JTextField tf = field();
        tf.setText(hint);
        tf.setForeground(TXT_DIM);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(hint)) { tf.setText(""); tf.setForeground(TXT); }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(hint); tf.setForeground(TXT_DIM); }
            }
        });
        return tf;
    }

    static String queryOf(JTextField tf) {
        if (tf == null) return "";
        return tf.getForeground().equals(TXT_DIM) ? "" : tf.getText().trim();
    }

    static JCheckBox cyberCheck(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(F_MONO_S);
        cb.setForeground(TXT);
        cb.setFocusPainted(false);
        cb.setOpaque(false);
        cb.setIcon(new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = ((JCheckBox) c).isSelected();
                g2.setColor(BG_FIELD);
                g2.fillRect(x, y, 15, 15);
                g2.setColor(sel ? NEON_GRN : withAlpha(TXT_DIM, 170));
                g2.drawRect(x, y, 14, 14);
                if (sel) {
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(x + 3, y + 7, x + 6, y + 11);
                    g2.drawLine(x + 6, y + 11, x + 12, y + 3);
                }
                g2.dispose();
            }
            public int getIconWidth() { return 20; }
            public int getIconHeight() { return 19; }
        });
        return cb;
    }

    static JButton chip(String text, Color c) {
        CyberButton b = new CyberButton(text, c, false);
        b.setFont(F_MONO_S);
        b.setMargin(new Insets(3, 9, 3, 9));
        return b;
    }

    static void addFormRow(JPanel p, int row, String name, JComponent comp) {
        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0; a.gridy = row; a.anchor = GridBagConstraints.WEST; a.insets = new Insets(0, 0, 14, 16);
        p.add(label(name, F_MONO_S, TXT_DIM), a);
        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 1; b.gridy = row; b.fill = GridBagConstraints.HORIZONTAL; b.weightx = 1;
        b.insets = new Insets(0, 0, 14, 0);
        p.add(comp, b);
    }

    static JPanel row(String name, JComponent value, JComponent actions) {
        JPanel r = new JPanel(new BorderLayout(12, 0));
        r.setOpaque(false);
        JLabel l = label(name, F_MONO_S, TXT_DIM);
        l.setPreferredSize(new Dimension(92, 18));
        r.add(l, BorderLayout.WEST);
        r.add(value, BorderLayout.CENTER);
        if (actions != null) r.add(actions, BorderLayout.EAST);
        return r;
    }

    static JPanel actsOf(JComponent... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        p.setOpaque(false);
        for (JComponent c : comps) p.add(c);
        return p;
    }

    static JPanel sectionHeader(String title, String sub, Color accent) {
        JPanel h = new JPanel(new BorderLayout(0, 8));
        h.setOpaque(false);
        JPanel col = new JPanel(new GridLayout(0, 1, 0, 4));
        col.setOpaque(false);
        col.add(label(title, F_TITLE, TXT));
        col.add(label(sub, F_MONO_S, TXT_DIM));
        h.add(col, BorderLayout.CENTER);
        JPanel line = new JPanel();
        line.setBackground(accent);
        line.setPreferredSize(new Dimension(0, 2));
        h.add(line, BorderLayout.SOUTH);
        return h;
    }

    static JPanel emptyState(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(LINE, 8, 6), empty(46)));
        p.add(label(msg, F_MONO, TXT_DIM), new GridBagConstraints());
        return p;
    }

    static String mask(int n) {
        int c = Math.max(6, Math.min(n, 24));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c; i++) sb.append('\u2022');
        return sb.toString();
    }

    static String cut(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 1) + "\u2026";
    }

    static List<String> parseTags(String s) {
        List<String> out = new ArrayList<>();
        for (String part : s.split(",")) {
            String t = part.trim().replaceFirst("^#", "");
            if (!t.isEmpty() && !out.contains(t)) out.add(t);
        }
        return out;
    }

    static String tagsStr(List<String> tags) {
        return tags == null ? "" : String.join(" ", tags);
    }

    static String fmtDate(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(ms));
    }

    static void styleScroll(JScrollPane sp) {
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            protected JButton createDecreaseButton(int o) { return zero(); }
            protected JButton createIncreaseButton(int o) { return zero(); }
            private JButton zero() {
                JButton b = new JButton();
                Dimension z = new Dimension(0, 0);
                b.setPreferredSize(z); b.setMinimumSize(z); b.setMaximumSize(z);
                return b;
            }
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x333A5C));
                g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 5, 5);
                g2.dispose();
            }
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(BG); g.fillRect(r.x, r.y, r.width, r.height);
            }
        });
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, Integer.MAX_VALUE));
        sp.getVerticalScrollBar().setBackground(BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getViewport().setBackground(BG);
    }

    static JScrollPane cyberScroll(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScroll(sp);
        return sp;
    }



    /* CUSTOM COMPONENTS */
    static class CyberButton extends JButton {
        final Color accent;
        final boolean filled;
        CyberButton(String text, Color accent, boolean filled) {
            super(text);
            this.accent = accent;
            this.filled = filled;
            setFont(F_MONO_B);
            setForeground(filled ? BG : accent);
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(4, 14, 4, 14));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { repaint(); }
                public void mouseExited(MouseEvent e) { repaint(); }
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            boolean hover = getModel().isRollover(), pressed = getModel().isPressed();
            if (filled) {
                g2.setColor(pressed ? accent.brighter() : hover ? accent : shade(accent, 0.82f));
                g2.fillRect(0, 0, w, h);
            } else {
                if (hover || pressed) {
                    g2.setColor(withAlpha(accent, 24));
                    g2.fillRect(1, 1, w - 2, h - 2);
                }
                g2.setColor(hover || pressed ? accent : withAlpha(accent, 130));
                g2.drawRect(0, 0, w - 1, h - 1);
                g2.setColor(accent);
                int t = 6;
                g2.drawLine(0, 0, t, 0);           g2.drawLine(0, 0, 0, t);
                g2.drawLine(w - 1 - t, 0, w - 1, 0); g2.drawLine(w - 1, 0, w - 1, t);
                g2.drawLine(0, h - 1 - t, 0, h - 1); g2.drawLine(0, h - 1, t, h - 1);
                g2.drawLine(w - 1 - t, h - 1, w - 1, h - 1); g2.drawLine(w - 1, h - 1 - t, w - 1, h - 1);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class NavButton extends JButton {
        final Color accent;
        boolean active;
        NavButton(String text, Color accent) {
            super(text);
            this.accent = accent;
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(F_MONO_B);
            setForeground(TXT_DIM);
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 42));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { if (!active) setForeground(TXT); }
                public void mouseExited(MouseEvent e)  { if (!active) setForeground(TXT_DIM); }
            });
        }
        void setActive(boolean s) {
            active = s;
            setForeground(s ? accent : TXT_DIM);
            repaint();
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(withAlpha(accent, 26));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(accent);
                g2.fillRect(0, 0, 3, getHeight());
            } else if (getModel().isRollover()) {
                g2.setColor(withAlpha(accent, 14));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class StrengthMeter extends JPanel {
        float level; Color color = NEON_PINK;
        StrengthMeter() { setPreferredSize(new Dimension(0, 14)); setBackground(BG_FIELD); }
        void setBits(double bits) {
            level = (float) Math.min(1.0, bits / 128.0);
            color = bits < 45 ? NEON_PINK : bits < 70 ? NEON_YEL : bits < 100 ? NEON_GRN : NEON_CYAN;
            repaint();
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            int segs = 26, gap = 3;
            double sw = (double) (w - gap * (segs - 1)) / segs;
            int filled = Math.round(level * segs);
            for (int i = 0; i < segs; i++) {
                int x = (int) (i * (sw + gap));
                g2.setColor(i < filled ? color : LINE);
                g2.fillRect(x, 2, (int) sw, h - 4);
            }
            g2.dispose();
        }
    }

    static class HexLogo extends JPanel {
        HexLogo(int size) {
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h / 2;
            int r = Math.min(w, h) / 2 - 4;
            Polygon hex = new Polygon();
            for (int i = 0; i < 6; i++) {
                double a = Math.PI / 3 * i - Math.PI / 2;
                hex.addPoint(cx + (int) (r * Math.cos(a)), cy + (int) (r * Math.sin(a)));
            }
            for (int i = 5; i >= 1; i--) {
                g2.setColor(withAlpha(NEON_CYAN, 14 + (5 - i) * 6));
                g2.setStroke(new BasicStroke(i * 2.4f));
                g2.draw(hex);
            }
            g2.setColor(NEON_CYAN);
            g2.setStroke(new BasicStroke(1.8f));
            g2.draw(hex);
            float kr = r * 0.30f;
            g2.setColor(NEON_PINK);
            g2.setStroke(new BasicStroke(Math.max(1.6f, r * 0.07f), BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
            g2.drawOval((int) (cx - kr / 2), (int) (cy - r * 0.42), (int) kr, (int) kr);
            int stemTop = (int) (cy - r * 0.42 + kr);
            g2.drawLine(cx, stemTop, cx, (int) (cy + r * 0.45));
            g2.drawLine(cx, (int) (cy + r * 0.28), (int) (cx + r * 0.18), (int) (cy + r * 0.28));
            g2.dispose();
        }
    }

    static class GridBG extends JPanel {
        javax.swing.Timer rain;
        GridBG() {
            setBackground(BG);
            rain = new javax.swing.Timer(66, ev -> repaint());
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, BG, w, h, BG_GRAD));
            g2.fillRect(0, 0, w, h);
            if (matrixRain) {
                g2.setFont(F_MONO_S);
                long t = System.currentTimeMillis() / 50;
                for (int x = 0; x < w; x += 14) {
                    int speed = 1 + (x * 7919 % 5);
                    int yOff = (int) ((t * speed) % (h + 120)) - 60;
                    for (int k = 0; k < 6; k++) {
                        int y = yOff - k * 16;
                        if (y < 0 || y > h) continue;
                        char ch = (char) ('!' + ((x * 31 + k * 17 + (int) (t / 40)) % 94));
                        g2.setColor(withAlpha(NEON_GRN, Math.max(20, 150 - k * 24)));
                        g2.drawString(String.valueOf(ch), x, y);
                    }
                }
                if (!rain.isRunning()) rain.start();
            } else {
                if (rain.isRunning()) rain.stop();
            }
            g2.setColor(withAlpha(NEON_CYAN, 12));
            for (int x = 0; x <= w; x += 42) g2.drawLine(x, 0, x, h);
            for (int y = 0; y <= h; y += 42) g2.drawLine(0, y, w, y);
            g2.dispose();
        }
    }

    static class ScrollGrid extends JPanel implements javax.swing.Scrollable {
        ScrollGrid(java.awt.LayoutManager lm) { super(lm); setOpaque(false); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        public boolean getScrollableTracksViewportWidth() { return true; }   // عرض = عرض viewport
        public boolean getScrollableTracksViewportHeight() { return false; } // ارتفاع = preferred → اسکرول عمودی
    }


    /* DATA & CRYPTO */
    static class PasswordEntry implements Serializable {
        static final long serialVersionUID = 1L;
        String title = "", username = "", password = "", url = "", notes = "";
        List<String> tags = new ArrayList<>();
        boolean favorite = false;
        long created = System.currentTimeMillis();
    }

    static class TokenEntry implements Serializable {
        static final long serialVersionUID = 1L;
        String name = "", token = "", notes = "";
        List<String> tags = new ArrayList<>();
        boolean favorite = false;
        long created = System.currentTimeMillis();
    }

    static class VaultData implements Serializable {
        static final long serialVersionUID = 1L;
        List<PasswordEntry> passwords = new ArrayList<>();
        List<TokenEntry> tokens = new ArrayList<>();
    }

     // VAULT MANAGER — manages multiple vaults with config file
    static class VaultManager {
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

         // مهاجرت v1.4 → v1.5: کپی vault.dat قدیمی به default.vault
         void migrateOldVault() throws Exception {
             Path old = dir.resolve("vault.dat");
             Path def = dir.resolve("default.vault");
             if (Files.exists(old) && !Files.exists(def) && !Files.exists(configFile)) {
                 Files.copy(old, def);
             }
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
             return manager.active.data;
         }

         VaultData initExisting(String name, char[] master) throws Exception {
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

         VaultData openVault(String name, char[] master) throws Exception {
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
             return manager.active.data;
         }

         void deleteVault(String name) throws Exception {
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

         void renameVault(String oldName, String newName) throws Exception {
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

         void setTheme(String theme) throws Exception {
             this.theme = theme;
             saveConfig();
         }

         List<String> getVaultNames() {
             List<String> names = new ArrayList<>();
             for (VaultInfo v : vaults) names.add(v.name);
             return names;
         }
     }

    static class VaultInfo {
        String name, file;
        VaultInfo(String name, String file) { this.name = name; this.file = file; }
    }

    static class JsonObject {
        Map<String, Object> map = new HashMap<>();
        String getString(String key) { return getString(key, null); }
        String getString(String key, String def) {
            Object v = map.get(key);
            return v instanceof String ? (String) v : def;
        }
        List<JsonObject> getArray(String key) {
            Object v = map.get(key);
            return v instanceof List ? (List<JsonObject>) v : new ArrayList<>();
        }
    }

    static JsonObject parseJson(String json) {
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

    static List<JsonObject> parseJsonArray(String arr) {
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

    static int findMatchingBracket(String s, char open, char close) {
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

    static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    static class Vault {
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

        boolean exists() { return Files.exists(file); }

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
                key = null; data = null;
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

        void lock() { key = null; data = null; salt = null; }

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

    static class Theme {
        final String name, displayName;
        final Color bg, bgPanel, bgCard, bgField;
        final Color line, neonCyan, neonPink, neonPurp, neonGrn, neonYel;
        final Color txt, txtDim, bgGrad, dim1, dim2, scrollC;
        final boolean matrixRain;

        Theme(String name, String displayName,
              Color bg, Color bgPanel, Color bgCard, Color bgField,
              Color line, Color neonCyan, Color neonPink, Color neonPurp, Color neonGrn, Color neonYel,
              Color txt, Color txtDim, Color bgGrad, Color dim1, Color dim2, Color scrollC,
              boolean matrixRain) {
            this.name = name; this.displayName = displayName;
            this.bg = bg; this.bgPanel = bgPanel; this.bgCard = bgCard; this.bgField = bgField;
            this.line = line; this.neonCyan = neonCyan; this.neonPink = neonPink;
            this.neonPurp = neonPurp; this.neonGrn = neonGrn; this.neonYel = neonYel;
            this.txt = txt; this.txtDim = txtDim; this.bgGrad = bgGrad;
            this.dim1 = dim1; this.dim2 = dim2; this.scrollC = scrollC;
            this.matrixRain = matrixRain;
        }
    }

    static final Theme[] PRESETS = {
        new Theme("cyberpunk", "Cyberpunk",
            new Color(0x0A0A14), new Color(0x10101E), new Color(0x151528), new Color(0x0C0C1A),
            new Color(0x2A2F4A), new Color(0x00F0FF), new Color(0xFF2A6D), new Color(0x9D4EFF),
            new Color(0x39FF14), new Color(0xFFE600), new Color(0xE4E9FF), new Color(0x7A82A8),
            new Color(0x16, 0x0B, 0x26), new Color(0x555C82), new Color(0x454B6E), new Color(0x333A5C),
            false),
        new Theme("matrix", "Matrix",
            new Color(0x000A00), new Color(0x001400), new Color(0x001C00), new Color(0x000E00),
            new Color(0x1E4D1E), new Color(0x00FF41), new Color(0x00CC33), new Color(0x66FF99),
            new Color(0x00FF41), new Color(0xB3FFB3), new Color(0xD6FFD6), new Color(0x4E994E),
            new Color(0x001400), new Color(0x3E8A3E), new Color(0x2E662E), new Color(0x1E4D1E),
            true),
        new Theme("dark", "Dark",
            new Color(0x1A1A1A), new Color(0x222222), new Color(0x2A2A2A), new Color(0x1E1E1E),
            new Color(0x3A3A3A), new Color(0x5DADE2), new Color(0xE74C3C), new Color(0x9B59B6),
            new Color(0x27AE60), new Color(0xF39C12), new Color(0xECF0F1), new Color(0x95A5A6),
            new Color(0x1A1A1A), new Color(0x7F8C8D), new Color(0x616161), new Color(0x4A4A4A),
            false),
        new Theme("light", "Light",
            new Color(0xF5F5F5), new Color(0xFFFFFF), new Color(0xFAFAFA), new Color(0xEFEFEF),
            new Color(0xDCDCDC), new Color(0x0099CC), new Color(0xCC3366), new Color(0x7733CC),
            new Color(0x2D8844), new Color(0xCC9900), new Color(0x1A1A1A), new Color(0x666666),
            new Color(0xE8E8E8), new Color(0x999999), new Color(0xAAAAAA), new Color(0xBBBBBB),
            false)
//        new Theme()
    };

    static Theme findTheme(String name) {
        for (Theme t : PRESETS) if (t.name.equals(name)) return t;
        return PRESETS[0];
    }
}
