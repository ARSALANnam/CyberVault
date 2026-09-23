package ui;

import ui.components.CyberButton;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.awt.Window;
import java.awt.Point;

import static ui.theme.ThemeManager.*;

public class UIUtils {

    public static Timer clipClear;

    public static JLabel label(String s, Font f, Color c) {
        JLabel l = new JLabel(s); l.setFont(f); l.setForeground(c); return l;
    }

    public static Border empty(int t, int l, int b, int r) {
        return BorderFactory.createEmptyBorder(t, l, b, r);
    }
    public static Border empty(int v) {
        return BorderFactory.createEmptyBorder(v, v, v, v);
    }

    public static void styleField(JTextField tf) {
        tf.setBackground(BG_FIELD);
        tf.setForeground(TXT);
        tf.setCaretColor(NEON_CYAN);
        tf.setFont(F_MONO);
        Border pad = BorderFactory.createEmptyBorder(9, 12, 9, 12);
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

    public static JTextField field() { JTextField tf = new JTextField(); styleField(tf); return tf; }
    public static JPasswordField passField() { JPasswordField pf = new JPasswordField(); styleField(pf); return pf; }

    public static JTextArea area() {
        JTextArea ta = new JTextArea();
        ta.setBackground(BG_FIELD); ta.setForeground(TXT); ta.setCaretColor(NEON_CYAN);
        ta.setFont(F_MONO); ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setBorder(empty(8, 10, 8, 10));
        return ta;
    }

    public static JTextField searchField(String hint) {
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

    public static String queryOf(JTextField tf) {
        if (tf == null) return "";
        return tf.getForeground().equals(TXT_DIM) ? "" : tf.getText().trim();
    }

    public static JCheckBox cyberCheck(String text) {
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

    public static JButton chip(String text, Color c) {
        CyberButton b = new CyberButton(text, c, false);
        b.setFont(F_MONO_S);
        b.setMargin(new Insets(3, 9, 3, 9));
        return b;
    }

    public static void addFormRow(JPanel p, int row, String name, JComponent comp) {
        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0; a.gridy = row; a.anchor = GridBagConstraints.WEST; a.insets = new Insets(0, 0, 14, 16);
        p.add(label(name, F_MONO_S, TXT_DIM), a);
        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 1; b.gridy = row; b.fill = GridBagConstraints.HORIZONTAL; b.weightx = 1;
        b.insets = new Insets(0, 0, 14, 0);
        p.add(comp, b);
    }

    public static JPanel row(String name, JComponent value, JComponent actions) {
        JPanel r = new JPanel(new BorderLayout(12, 0));
        r.setOpaque(false);
        JLabel l = label(name, F_MONO_S, TXT_DIM);
        l.setPreferredSize(new Dimension(92, 18));
        r.add(l, BorderLayout.WEST);
        r.add(value, BorderLayout.CENTER);
        if (actions != null) r.add(actions, BorderLayout.EAST);
        return r;
    }

    public static JPanel actsOf(JComponent... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        p.setOpaque(false);
        for (JComponent c : comps) p.add(c);
        return p;
    }

    public static JPanel sectionHeader(String title, String sub, Color accent) {
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

    public static JPanel emptyState(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(LINE, 8, 6), empty(46)));
        p.add(label(msg, F_MONO, TXT_DIM), new GridBagConstraints());
        return p;
    }

    public static String mask(int n) {
        int c = Math.max(6, Math.min(n, 24));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c; i++) sb.append('\u2022');
        return sb.toString();
    }

    public static String cut(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 1) + "\u2026";
    }

    public static List<String> parseTags(String s) {
        List<String> out = new ArrayList<>();
        for (String part : s.split(",")) {
            String t = part.trim().replaceFirst("^#", "");
            if (!t.isEmpty() && !out.contains(t)) out.add(t);
        }
        return out;
    }

    public static String tagsStr(List<String> tags) {
        return tags == null ? "" : String.join(" ", tags);
    }

    public static String fmtDate(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(ms));
    }

    public static void styleScroll(JScrollPane sp) {
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

    public static JScrollPane cyberScroll(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScroll(sp);
        return sp;
    }

    public static void copyText(String s, JButton src) {
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

    public static JButton copyChip(String secret) {
        JButton b = chip("COPY", NEON_GRN);
        b.addActionListener(ev -> copyText(secret, b));
        return b;
    }

    public static void openUrl(String url) {
        try {
            String u = url.startsWith("http") ? url : "https://" + url;
            Desktop.getDesktop().browse(new URI(u));
        } catch (Exception ignored) {}
    }

    public static void escapeToClose(JDialog d) {
        d.getRootPane().registerKeyboardAction(ev -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static JButton miniBtn(String txt, ActionListener al, Color hover) {
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

    public static MouseAdapter windowDrag(Window w) {
        return new MouseAdapter() {
            Point start, winStart;
            public void mousePressed(MouseEvent e) { start = e.getLocationOnScreen(); winStart = w.getLocation(); }
            public void mouseDragged(MouseEvent e) {
                Point p = e.getLocationOnScreen();
                w.setLocation(winStart.x + p.x - start.x, winStart.y + p.y - start.y);
            }
        };
    }

    public static JDialog cyberDialog(String title, Color accent) {
        JDialog d = new JDialog((Frame)null, true);
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

    public static boolean confirmAction(String msg) {
        JDialog d = cyberDialog("CONFIRM ACTION", NEON_PINK);
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(24, 26, 24, 26));
        body.add(label("<html>" + msg + "</html>", F_MONO, TXT), BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        boolean[] r = { false };
        ui.components.CyberButton yes = new ui.components.CyberButton("CONFIRM", NEON_PINK, true);
        ui.components.CyberButton no = new ui.components.CyberButton("CANCEL", NEON_CYAN, false);
        yes.addActionListener(ev -> { r[0] = true; d.dispose(); });
        no.addActionListener(ev -> d.dispose());
        btns.add(yes); btns.add(no);
        body.add(btns, BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.setSize(440, 150);
        d.setLocationRelativeTo(null);
        escapeToClose(d);
        d.setVisible(true);
        return r[0];
    }

    public static void alert(String msg) {
        JDialog d = cyberDialog("SYSTEM ALERT", NEON_PINK);
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(24, 26, 24, 26));
        body.add(label(msg, F_MONO, TXT), BorderLayout.CENTER);
        ui.components.CyberButton ok = new ui.components.CyberButton("OK", NEON_CYAN, true);
        ok.addActionListener(ev -> d.dispose());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btns.setOpaque(false); btns.add(ok);
        body.add(btns, BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.setSize(420, 150);
        d.setLocationRelativeTo(null);
        escapeToClose(d);
        d.setVisible(true);
    }
}
