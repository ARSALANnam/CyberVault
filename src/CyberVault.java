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