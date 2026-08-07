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

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;



public class CyberVault extends JFrame {

    /* THEME */
    static final Color BG = new Color(0x0A0A14);
    static final Color BG_PANEL = new Color(0x10101E);
    static final Color BG_CARD = new Color(0x151528);
    static final Color BG_FIELD = new Color(0x0C0C1A);
    static final Color LINE = new Color(0x2A2F4A);
    static final Color NEON_CYAN = new Color(0x00F0FF);
    static final Color NEON_PINK = new Color(0xFF2A6D);
    static final Color NEON_PURP = new Color(0x9D4EFF);
    static final Color NEON_GRN = new Color(0x39FF14);
    static final Color NEON_YEL = new Color(0xFFE600);
    static final Color TXT = new Color(0xE4E9FF);
    static final Color TXT_DIM = new Color(0x7A82A8);

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

    final Vault vault = new Vault();
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

    JTextField genOut; JSlider genLen; JLabel genLenVal, meterLabel;
    JCheckBox gUp, gLo, gDg, gSy, gAmb;
    StrengthMeter meter;

    Timer clipClear;





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

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(shade(NEON_CYAN, 0.55f)));
        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(screenHolder, BorderLayout.CENTER);
        setContentPane(root);

        screenHolder.setBackground(BG);
        screenHolder.add(buildAuthScreen(), "AUTH");
        screenHolder.add(buildAppScreen(), "APP");
        configureAuthMode();
        screens.layout.show(screenHolder, "AUTH");
        initTray();
    }

    static class CardLayoutScreens { java.awt.CardLayout layout = new java.awt.CardLayout(); }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        boolean hidden = args.length > 0 && "--hidden".equals(args[0]);
        SwingUtilities.invokeLater(() -> {
            CyberVault cv = new CyberVault();
            cv.setVisible(!hidden);
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

    void configureAuthMode() {
        boolean first = !vault.exists();
        authTitle.setText(first ? "INITIALIZE VAULT" : "ACCESS VAULT");
        authTitle.setForeground(first ? NEON_YEL : NEON_GRN);
        authSub.setText(first ? "// first run \u2014 create a master key (not recoverable)"
                : "// enter master key to decrypt");
        authLbl2.setVisible(first); authPass2.setVisible(first);
        authBtn.setText(first ? "\u25B6 INITIALIZE VAULT" : "\u25B6 ACCESS VAULT");
        authStatus.setText(" ");
        authPass.setText(""); authPass2.setText("");
        authCard.revalidate(); authCard.repaint();
    }

    void authAction() {
        char[] p1 = authPass.getPassword();
        char[] p2 = authPass2.getPassword();
        try {
            if (vault.exists()) {
                authStatus.setText("// decrypting\u2026");
                if (vault.unlock(p1)) enterApp();
                else { authStatus.setText("\u2715 ACCESS DENIED \u2014 WRONG MASTER KEY"); flashAuthError(); }
            } else {
                if (p1.length < 6) { authStatus.setText("\u2715 MASTER KEY TOO SHORT (MIN 6)"); return; }
                if (!Arrays.equals(p1, p2)) { authStatus.setText("\u2715 KEYS DO NOT MATCH"); return; }
                vault.create(p1);
                enterApp();
            }
        } catch (Exception ex) {
            authStatus.setText("\u2715 ERROR: " + ex.getClass().getSimpleName());
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
        refreshPasswords(); refreshTokens(); updateStats();
        selectNav(navPass, "PASS");
        screens.layout.show(screenHolder, "APP");
    }

    void lockVault() {
        vault.lock();
        configureAuthMode();
        screens.layout.show(screenHolder, "AUTH");
    }

    /* TRAY */
    void initTray() {
        if (!SystemTray.isSupported()) return;
        try {
            java.net.URL u = CyberVault.class.getResource("/tray.png");
            if (u == null) u = CyberVault.class.getResource("/icon.png");
            if (u == null) return;
            java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(u);

            // برش خودکار حاشیه روشن دور مربع سیاه
            int x0 = src.getWidth(), y0 = src.getHeight(), x1 = 0, y1 = 0;
            for (int y = 0; y < src.getHeight(); y++)
                for (int x = 0; x < src.getWidth(); x++) {
                    int rgb = src.getRGB(x, y);
                    if (((rgb >> 16) & 255) + ((rgb >> 8) & 255) + (rgb & 255) < 300) {
                        if (x < x0) x0 = x; if (x > x1) x1 = x;
                        if (y < y0) y0 = y; if (y > y1) y1 = y;
                    }
                }
            if (x1 > x0 && y1 > y0)
                src = src.getSubimage(x0, y0, x1 - x0 + 1, y1 - y0 + 1);

            int size = 16;   // اگه کوچیک یا crop دیدی: 48
            java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = icon.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(src, 0, 0, size, size, null);
            g2.dispose();

            PopupMenu menu = new PopupMenu();
            MenuItem open = new MenuItem("Open CyberVault");
            open.addActionListener(ev -> showWindow());
            MenuItem lock = new MenuItem("Lock Vault");
            lock.addActionListener(ev -> { lockVault(); showWindow(); });
            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(ev -> System.exit(0));
            menu.add(open); menu.add(lock); menu.addSeparator(); menu.add(exit);

            TrayIcon ti = new TrayIcon(icon, "CyberVault", menu);
            ti.setImageAutoSize(false);
            ti.addActionListener(ev -> showWindow());
            SystemTray.getSystemTray().add(ti);
        } catch (Exception ignored) {}
    }


    void showWindow() {
        setVisible(true);
        setExtendedState(JFrame.NORMAL);
        toFront();
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
        txts.add(label("v1.3.0 // SECURE", F_MONO_S, TXT_DIM));
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
        statsLabel = label("\u2026", F_MONO_S, TXT_DIM);
        JLabel foot = label("LOCAL ONLY // NO CLOUD", F_MONO_S, new Color(0x454B6E));
        JPanel sp = new JPanel(new GridLayout(0, 1, 0, 5));
        sp.setOpaque(false);
        sp.add(statsLabel); sp.add(foot);
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
        int p = vault.data == null ? 0 : vault.data.passwords.size();
        int t = vault.data == null ? 0 : vault.data.tokens.size();
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
        JPanel addWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        addWrap.setOpaque(false); addWrap.add(add);
        head.add(addWrap, BorderLayout.EAST);

        passSearch = searchField("SEARCH ENTRIES\u2026");
        passSearch.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { refreshPasswords(); } });

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(passSearch, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);

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
        if (vault.data != null) {
            for (PasswordEntry e : vault.data.passwords) {
                if (!q.isEmpty() && !((e.title + " " + e.username + " " + e.url).toLowerCase().contains(q))) continue;
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
        JButton edit = chip("EDIT", NEON_CYAN);
        JButton del = chip("DEL", NEON_PINK);
        edit.addActionListener(ev -> openPasswordDialog(e));
        del.addActionListener(ev -> {
            if (confirmAction("DELETE \"" + e.title.toUpperCase() + "\" PERMANENTLY?")) {
                vault.data.passwords.remove(e);
                saveVault(); refreshPasswords();
            }
        });
        acts.add(edit); acts.add(del);
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

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    void openPasswordDialog(PasswordEntry ex) {
        JDialog d = cyberDialog(ex == null ? "NEW PASSWORD ENTRY" : "EDIT ENTRY", NEON_CYAN);
        JTextField fTitle = field(), fUser = field(), fUrl = field();
        JPasswordField fPass = passField();
        JTextArea fNotes = area();
        if (ex != null) {
            fTitle.setText(ex.title); fUser.setText(ex.username);
            fPass.setText(ex.password); fUrl.setText(ex.url); fNotes.setText(ex.notes);
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
        addFormRow(form, 4, "NOTES", ns);

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
            if (ex == null) vault.data.passwords.add(ent);
            if (saveVault()) { refreshPasswords(); d.dispose(); }
        });
        cancel.addActionListener(ev -> d.dispose());
        btns.add(save); btns.add(cancel);
        foot.add(btns, BorderLayout.SOUTH);
        body.add(foot, BorderLayout.SOUTH);

        d.add(body, BorderLayout.CENTER);
        d.setSize(560, 470);
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
        JPanel addWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        addWrap.setOpaque(false); addWrap.add(add);
        head.add(addWrap, BorderLayout.EAST);

        tokSearch = searchField("SEARCH TOKENS\u2026");
        tokSearch.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { refreshTokens(); } });

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(tokSearch, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);

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
        if (vault.data != null) {
            for (TokenEntry e : vault.data.tokens) {
                if (!q.isEmpty() && !((e.name + " " + e.notes).toLowerCase().contains(q))) continue;
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
        JButton edit = chip("EDIT", NEON_CYAN);
        JButton del = chip("DEL", NEON_PINK);
        edit.addActionListener(ev -> openTokenDialog(e));
        del.addActionListener(ev -> {
            if (confirmAction("DELETE TOKEN \"" + e.name.toUpperCase() + "\" PERMANENTLY?")) {
                vault.data.tokens.remove(e);
                saveVault(); refreshTokens();
            }
        });
        acts.add(edit); acts.add(del);
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

        card.add(body, BorderLayout.CENTER);
        return card;
    }


    void openTokenDialog(TokenEntry ex) {
        JDialog d = cyberDialog(ex == null ? "NEW API TOKEN" : "EDIT API TOKEN", NEON_PURP);
        JTextField fName = field();
        JPasswordField fTok = passField();
        JTextArea fNotes = area();
        if (ex != null) { fName.setText(ex.name); fTok.setText(ex.token); fNotes.setText(ex.notes); }
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
        addFormRow(form, 2, "NOTES", ns);

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
            if (ex == null) vault.data.tokens.add(ent);
            if (saveVault()) { refreshTokens(); d.dispose(); }
        });
        cancel.addActionListener(ev -> d.dispose());
        btns.add(save); btns.add(cancel);
        foot.add(btns, BorderLayout.SOUTH);
        body.add(foot, BorderLayout.SOUTH);

        d.add(body, BorderLayout.CENTER);
        d.setSize(520, 390);
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

    boolean saveVault() {
        try { vault.save(); return true; }
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
        GridBG() { setBackground(BG); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, BG, w, h, new Color(0x16, 0x0B, 0x26)));
            g2.fillRect(0, 0, w, h);
            g2.setColor(withAlpha(NEON_CYAN, 12));
            for (int x = 0; x <= w; x += 42) g2.drawLine(x, 0, x, h);
            for (int y = 0; y <= h; y += 42) g2.drawLine(0, y, w, y);
            g2.dispose();
        }
    }


    /* DATA & CRYPTO */
    static class PasswordEntry implements Serializable {
        static final long serialVersionUID = 1L;
        String title = "", username = "", password = "", url = "", notes = "";
        long created = System.currentTimeMillis();
    }

    static class TokenEntry implements Serializable {
        static final long serialVersionUID = 1L;
        String name = "", token = "", notes = "";
        long created = System.currentTimeMillis();
    }

    static class VaultData implements Serializable {
        static final long serialVersionUID = 1L;
        List<PasswordEntry> passwords = new ArrayList<>();
        List<TokenEntry> tokens = new ArrayList<>();
    }

    static class Vault {
        final Path file;
        byte[] salt;
        SecretKey key;
        VaultData data;

        Vault() {
            file = Paths.get(System.getProperty("user.home"), ".cybervault", "vault.dat");
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
}
