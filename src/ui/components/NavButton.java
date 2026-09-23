package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static ui.theme.ThemeManager.*;

public class NavButton extends JButton {
    private final Color accent;
    private boolean active;

    public NavButton(String text, Color accent) {
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

    public void setActive(boolean s) {
        active = s;
        setForeground(s ? accent : TXT_DIM);
        repaint();
    }

    @Override
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
