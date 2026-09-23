package ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ThemeManager {
    public static Color BG = new Color(0x0A0A14);
    public static Color BG_PANEL = new Color(0x10101E);
    public static Color BG_CARD = new Color(0x151528);
    public static Color BG_FIELD = new Color(0x0C0C1A);
    public static Color LINE = new Color(0x2A2F4A);
    public static Color NEON_CYAN = new Color(0x00F0FF);
    public static Color NEON_PINK = new Color(0xFF2A6D);
    public static Color NEON_PURP = new Color(0x9D4EFF);
    public static Color NEON_GRN = new Color(0x39FF14);
    public static Color NEON_YEL = new Color(0xFFE600);
    public static Color TXT = new Color(0xE4E9FF);
    public static Color TXT_DIM = new Color(0x7A82A8);
    public static Color BG_GRAD = new Color(0x16, 0x0B, 0x26);
    public static Color DIM_1 = new Color(0x555C82);
    public static Color DIM_2 = new Color(0x454B6E);
    public static Color SCROLL_C = new Color(0x333A5C);
    public static boolean matrixRain = false;

    public static Font pickMono(int style, float size) {
        String[] prefs = {"Consolas", "JetBrains Mono", "Cascadia Code", "Fira Code", "Menlo", "DejaVu Sans Mono"};
        Set<String> avail = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String p : prefs) if (avail.contains(p)) return new Font(p, style, 12).deriveFont(size);
        return new Font(Font.MONOSPACED, style, 12).deriveFont(size);
    }

    public static final Font F_MONO   = pickMono(Font.PLAIN, 13f);
    public static final Font F_MONO_S = pickMono(Font.PLAIN, 11f);
    public static final Font F_MONO_B = pickMono(Font.BOLD, 13f);
    public static final Font F_TITLE  = pickMono(Font.BOLD, 20f);
    public static final Font F_BIG    = pickMono(Font.BOLD, 27f);

    public static void applyTheme(String name) {
        Theme t = findTheme(name);
        BG = t.bg; BG_PANEL = t.bgPanel; BG_CARD = t.bgCard; BG_FIELD = t.bgField;
        LINE = t.line; NEON_CYAN = t.neonCyan; NEON_PINK = t.neonPink;
        NEON_PURP = t.neonPurp; NEON_GRN = t.neonGrn; NEON_YEL = t.neonYel;
        TXT = t.txt; TXT_DIM = t.txtDim; BG_GRAD = t.bgGrad;
        DIM_1 = t.dim1; DIM_2 = t.dim2; SCROLL_C = t.scrollC;
        matrixRain = t.matrixRain;
    }

    public static Theme findTheme(String name) {
        for (Theme t : PRESETS) if (t.name.equals(name)) return t;
        return PRESETS[0];
    }

    public static final Theme[] PRESETS = {
        new Theme("cyberpunk", "Cyberpunk",
            new Color(0x0A0A14), new Color(0x10101E), new Color(0x151528), new Color(0x0C0C1A),
            new Color(0x2A2F4A), new Color(0x00F0FF), new Color(0xFF2A6D), new Color(0x9D4EFF),
            new Color(0x39FF14), new Color(0xFFE600), new Color(0xE4E9FF), new Color(0x7A82A8),
            new Color(0x16, 0x0B, 0x26), new Color(0x555C82), new Color(0x454B6E), new Color(0x333A5C),
            false),
        new Theme("matrix", "Matrix",
            new Color(0x000A00), new Color(0x001400), new Color(0x001C00), new Color(0x000E00),
            new Color(0x1E4D1E), new Color(0x00FF41), new Color(0x00CC33), new Color(0x66FF99),
            new Color(0x00FF41), new Color(0xB3FFB3), new Color(0xD6FFD6), new Color(0x4E994E),
            new Color(0x001400), new Color(0x3E8A3E), new Color(0x2E662E), new Color(0x1E4D1E),
            true),
        new Theme("dark", "Dark",
            new Color(0x1A1A1A), new Color(0x222222), new Color(0x2A2A2A), new Color(0x1E1E1E),
            new Color(0x3A3A3A), new Color(0x5DADE2), new Color(0xE74C3C), new Color(0x9B59B6),
            new Color(0x27AE60), new Color(0xF39C12), new Color(0xECF0F1), new Color(0x95A5A6),
            new Color(0x1A1A1A), new Color(0x7F8C8D), new Color(0x616161), new Color(0x4A4A4A),
            false),
        new Theme("light", "Light",
            new Color(0xF5F5F5), new Color(0xFFFFFF), new Color(0xFAFAFA), new Color(0xEFEFEF),
            new Color(0xDCDCDC), new Color(0x0099CC), new Color(0xCC3366), new Color(0x7733CC),
            new Color(0x2D8844), new Color(0xCC9900), new Color(0x1A1A1A), new Color(0x666666),
            new Color(0xE8E8E8), new Color(0x999999), new Color(0xAAAAAA), new Color(0xBBBBBB),
            false),
        new Theme("pufak", "Pufak Namaki",
            new Color(0xDA291C), new Color(0xB02015), new Color(0xBE2418), new Color(0xA01A0F),
            new Color(0xE8A33D), new Color(0xFFC72C), new Color(0xFFFFFF), new Color(0xFFB300),
            new Color(0xFFE082), new Color(0xFFEB3B), new Color(0xFFF8E1), new Color(0xEFA093),
            new Color(0x8C1A10), new Color(0xE58F7F), new Color(0xD07A6C), new Color(0xFFC72C),
            false)
    };

    public static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public static Color shade(Color c, float f) {
        return new Color((int) (c.getRed() * f), (int) (c.getGreen() * f), (int) (c.getBlue() * f));
    }
}
