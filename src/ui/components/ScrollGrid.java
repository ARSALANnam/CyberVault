package ui.components;

import javax.swing.*;
import java.awt.*;

public class ScrollGrid extends JPanel implements Scrollable {
    public ScrollGrid(LayoutManager lm) {
        super(lm);
        setOpaque(false);
    }
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
    public boolean getScrollableTracksViewportWidth() { return true; }
    public boolean getScrollableTracksViewportHeight() { return false; }
}
