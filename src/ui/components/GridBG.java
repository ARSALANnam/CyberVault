package ui.components;

import javax.swing.*;
import java.awt.*;
import static ui.theme.ThemeManager.*;

public class GridBG extends JPanel {
    private javax.swing.Timer rain;

    public GridBG() {
        setBackground(BG);
        rain = new javax.swing.Timer(66, ev -> repaint());
    }

    @Override
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
