package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static ui.theme.ThemeManager.*;

public class CyberButton extends JButton {
    private final Color accent;
    private final boolean filled;

    public CyberButton(String text, Color accent, boolean filled) {
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

    @Override
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
