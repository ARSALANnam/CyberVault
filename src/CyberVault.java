import javax.crypto.Chipher;
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
        } catch (Exception ignored) {}
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
    }

    static class CardLayoutScreens { java.awt.CardLayout layout = new java.awt.CardLayout(); }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new CyberVault().setVisible(true));
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