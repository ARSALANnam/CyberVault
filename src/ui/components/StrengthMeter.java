package ui.components;

import javax.swing.*;
import java.awt.*;
import static ui.theme.ThemeManager.*;

public class StrengthMeter extends JPanel {
    private float level;
    private Color color = NEON_PINK;

    public StrengthMeter() {
        setPreferredSize(new Dimension(0, 14));
        setBackground(BG_FIELD);
    }

    public void setBits(double bits) {
        level = (float) Math.min(1.0, bits / 128.0);
        color = bits < 45 ? NEON_PINK : bits < 70 ? NEON_YEL : bits < 100 ? NEON_GRN : NEON_CYAN;
        repaint();
    }

    @Override
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
