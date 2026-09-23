import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class CyberButton extends JButton {
    final Color accent; final boolean filled;
    CyberButton(String text, Color accent, boolean filled) {
        super(text); this.accent = accent; this.filled = filled;
        setFont(CyberVault.F_MONO_B);
        setForeground(filled ? CyberVault.BG : accent);
        setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(4, 14, 4, 14));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { repaint(); }
            public void mouseExited(MouseEvent e)  { repaint(); }
        });
    }
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean hover = getModel().isRollover(), pressed = getModel().isPressed();
        if (filled) {
            g2.setColor(pressed ? accent.brighter() : hover ? accent : CyberVault.shade(accent, 0.82f));
            g2.fillRect(0, 0, w, h);
        } else {
            if (hover || pressed) { g2.setColor(CyberVault.withAlpha(accent, 24)); g2.fillRect(1, 1, w - 2, h - 2); }
            g2.setColor(hover || pressed ? accent : CyberVault.withAlpha(accent, 130));
            g2.drawRect(0, 0, w - 1, h - 1);
            g2.setColor(accent); int t = 6;
            g2.drawLine(0, 0, t, 0); g2.drawLine(0, 0, 0, t);
            g2.drawLine(w - 1 - t, 0, w - 1, 0); g2.drawLine(w - 1, 0, w - 1, t);
            g2.drawLine(0, h - 1 - t, 0, h - 1); g2.drawLine(0, h - 1, t, h - 1);
            g2.drawLine(w - 1 - t, h - 1, w - 1, h - 1); g2.drawLine(w - 1, h - 1 - t, w - 1, h - 1);
        }
        g2.dispose(); super.paintComponent(g);
    }
}

class NavButton extends JButton {
    final Color accent; boolean active;
    NavButton(String text, Color accent) {
        super(text); this.accent = accent;
        setHorizontalAlignment(SwingConstants.LEFT); setFont(CyberVault.F_MONO_B);
        setForeground(CyberVault.TXT_DIM);
        setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, 42));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!active) setForeground(CyberVault.TXT); }
            public void mouseExited(MouseEvent e)  { if (!active) setForeground(CyberVault.TXT_DIM); }
        });
    }
    void setActive(boolean s) { active = s; setForeground(s ? accent : CyberVault.TXT_DIM); repaint(); }
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (active) {
            g2.setColor(CyberVault.withAlpha(accent, 26)); g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(accent); g2.fillRect(0, 0, 3, getHeight());
        } else if (getModel().isRollover()) {
            g2.setColor(CyberVault.withAlpha(accent, 14)); g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose(); super.paintComponent(g);
    }
}

class StrengthMeter extends JPanel {
    float level; Color color = CyberVault.NEON_PINK;
    StrengthMeter() { setPreferredSize(new Dimension(0, 14)); setBackground(CyberVault.BG_FIELD); }
    void setBits(double bits) {
        level = (float) Math.min(1.0, bits / 128.0);
        color = bits < 45 ? CyberVault.NEON_PINK : bits < 70 ? CyberVault.NEON_YEL : bits < 100 ? CyberVault.NEON_GRN : CyberVault.NEON_CYAN;
        repaint();
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight(), segs = 26, gap = 3;
        double sw = (double) (w - gap * (segs - 1)) / segs;
        int filled = Math.round(level * segs);
        for (int i = 0; i < segs; i++) {
            int x = (int) (i * (sw + gap));
            g2.setColor(i < filled ? color : CyberVault.LINE);
            g2.fillRect(x, 2, (int) sw, h - 4);
        }
        g2.dispose();
    }
}

class HexLogo extends JPanel {
    HexLogo(int size) { setPreferredSize(new Dimension(size, size)); setOpaque(false); }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), cx = w / 2, cy = h / 2;
        int r = Math.min(w, h) / 2 - 4;
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 3 * i - Math.PI / 2;
            hex.addPoint(cx + (int)(r * Math.cos(a)), cy + (int)(r * Math.sin(a)));
        }
        for (int i = 5; i >= 1; i--) {
            g2.setColor(CyberVault.withAlpha(CyberVault.NEON_CYAN, 14 + (5 - i) * 6));
            g2.setStroke(new BasicStroke(i * 2.4f)); g2.draw(hex);
        }
        g2.setColor(CyberVault.NEON_CYAN); g2.setStroke(new BasicStroke(1.8f)); g2.draw(hex);
        float kr = r * 0.30f;
        g2.setColor(CyberVault.NEON_PINK);
        g2.setStroke(new BasicStroke(Math.max(1.6f, r * 0.07f), BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
        g2.drawOval((int)(cx - kr/2), (int)(cy - r * 0.42), (int)kr, (int)kr);
        int stemTop = (int)(cy - r * 0.42 + kr);
        g2.drawLine(cx, stemTop, cx, (int)(cy + r * 0.45));
        g2.drawLine(cx, (int)(cy + r * 0.28), (int)(cx + r * 0.18), (int)(cy + r * 0.28));
        g2.dispose();
    }
}

class GridBG extends JPanel {
    javax.swing.Timer rain;
    GridBG() {
        setBackground(CyberVault.BG);
        rain = new javax.swing.Timer(66, ev -> repaint());
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, CyberVault.BG, w, h, CyberVault.BG_GRAD));
        g2.fillRect(0, 0, w, h);
        if (CyberVault.matrixRain) {
            g2.setFont(CyberVault.F_MONO_S);
            long t = System.currentTimeMillis() / 50;
            for (int x = 0; x < w; x += 14) {
                int speed = 1 + (x * 7919 % 5);
                int yOff = (int) ((t * speed) % (h + 120)) - 60;
                for (int k = 0; k < 6; k++) {
                    int y = yOff - k * 16;
                    if (y < 0 || y > h) continue;
                    char ch = (char) ('!' + ((x * 31 + k * 17 + (int) (t / 40)) % 94));
                    g2.setColor(CyberVault.withAlpha(CyberVault.NEON_GRN, Math.max(20, 150 - k * 24)));
                    g2.drawString(String.valueOf(ch), x, y);
                }
            }
            if (!rain.isRunning()) rain.start();
        } else {
            if (rain.isRunning()) rain.stop();
        }
        g2.setColor(CyberVault.withAlpha(CyberVault.NEON_CYAN, 12));
        for (int x = 0; x <= w; x += 42) g2.drawLine(x, 0, x, h);
        for (int y = 0; y <= h; y += 42) g2.drawLine(0, y, w, y);
        g2.dispose();
    }
}

class ScrollGrid extends JPanel implements javax.swing.Scrollable {
    ScrollGrid(java.awt.LayoutManager lm) { super(lm); setOpaque(false); }
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
    public boolean getScrollableTracksViewportWidth() { return true; }
    public boolean getScrollableTracksViewportHeight() { return false; }
}

class MiniChip extends JComponent {
    final String text; final Color color; final Color bg;
    MiniChip(String text, Color color, Color bg) {
        this.text = text; this.color = color; this.bg = bg;
        setPreferredSize(new Dimension(46, 16));
    }
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(bg); g2.fillRect(0, 0, w, h);
        g2.setColor(color); g2.drawRect(0, 0, w - 1, h - 1);
        g2.setFont(CyberVault.pickMono(java.awt.Font.BOLD, 8f));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        g2.drawString(text, (w - tw) / 2, (h + fm.getAscent() - 2) / 2);
        g2.dispose();
    }
}
