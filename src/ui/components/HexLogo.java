package ui.components;

import javax.swing.*;
import java.awt.*;
import static ui.theme.ThemeManager.*;

public class HexLogo extends JPanel {
    public HexLogo(int size) {
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
    }

    @Override
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
