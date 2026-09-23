package ui.panels;

import ui.components.CyberButton;
import ui.components.StrengthMeter;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.ItemListener;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static ui.UIUtils.*;
import static ui.theme.ThemeManager.*;

public class GeneratorPanel extends JPanel {
    private JTextField genOut;
    private JSlider genLen;
    private JLabel genLenVal;
    private JLabel meterLabel;
    private JCheckBox gUp, gLo, gDg, gSy, gAmb;
    private StrengthMeter meter;

    public GeneratorPanel() {
        setLayout(new GridBagLayout());
        setBackground(BG);
        setBorder(empty(22, 26, 20, 22));

        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), empty(24, 26, 26, 26)));
        card.add(sectionHeader("PASSWORD GENERATOR", "// forge fresh high-entropy keys", NEON_YEL), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;

        genOut = field();
        genOut.setFont(pickMono(Font.BOLD, 17f));
        g.gridy = 0; body.add(genOut, g);

        meter = new StrengthMeter();
        g.gridy = 1; g.insets = new Insets(14, 0, 4, 0);
        body.add(meter, g);
        meterLabel = label(" ", F_MONO_S, TXT_DIM);
        g.gridy = 2; g.insets = new Insets(0, 0, 18, 0);
        body.add(meterLabel, g);

        JPanel lenRow = new JPanel(new BorderLayout(14, 0));
        lenRow.setOpaque(false);
        genLenVal = label("18", F_MONO_B, NEON_YEL);
        genLenVal.setPreferredSize(new Dimension(34, 18));
        lenRow.add(label("LENGTH", F_MONO_S, TXT_DIM), BorderLayout.WEST);
        lenRow.add(genLenVal, BorderLayout.EAST);

        genLen = new JSlider(8, 64, 18);
        genLen.setOpaque(false);
        genLen.setFocusable(false);
        genLen.setUI(new BasicSliderUI(genLen) {
            public void paintTrack(Graphics gr) {
                Graphics2D g2 = (Graphics2D) gr.create();
                int y = trackRect.y + trackRect.height / 2 - 2;
                g2.setColor(LINE);
                g2.fillRect(trackRect.x, y, trackRect.width, 4);
                g2.setColor(withAlpha(NEON_YEL, 150));
                g2.fillRect(trackRect.x, y, Math.max(0, thumbRect.x - trackRect.x), 4);
                g2.dispose();
            }
            public void paintThumb(Graphics gr) {
                Graphics2D g2 = (Graphics2D) gr.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NEON_YEL);
                g2.fillRect(thumbRect.x + 3, thumbRect.y + 2, thumbRect.width - 6, thumbRect.height - 4);
                g2.dispose();
            }
            public void paintFocus(Graphics gr) {}
        });
        lenRow.add(genLen, BorderLayout.CENTER);
        g.gridy = 3; body.add(lenRow, g);

        gUp = cyberCheck("UPPERCASE  A-Z");
        gLo = cyberCheck("LOWERCASE  a-z");
        gDg = cyberCheck("NUMBERS  0-9");
        gSy = cyberCheck("SYMBOLS  !@#$%");
        gAmb = cyberCheck("NO AMBIGUOUS  O0lI1");
        gUp.setSelected(true); gLo.setSelected(true); gDg.setSelected(true); gSy.setSelected(true);

        JPanel opts = new JPanel(new GridLayout(0, 2, 10, 8));
        opts.setOpaque(false);
        opts.add(gUp); opts.add(gLo); opts.add(gDg); opts.add(gSy); opts.add(gAmb);
        g.gridy = 4; g.insets = new Insets(18, 0, 22, 0);
        body.add(opts, g);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        CyberButton genBtn = new CyberButton("\u26A1 GENERATE", NEON_YEL, true);
        genBtn.setPreferredSize(new Dimension(220, 44));
        CyberButton copyBtn = new CyberButton("COPY", NEON_GRN, false);
        copyBtn.setPreferredSize(new Dimension(120, 44));

        genBtn.addActionListener(ev -> regen());
        copyBtn.addActionListener(ev -> copyText(genOut.getText(), copyBtn));
        btnRow.add(genBtn); btnRow.add(copyBtn);
        g.gridy = 5;
        body.add(btnRow, g);

        card.add(body, BorderLayout.CENTER);

        genLen.addChangeListener(ev -> regen());
        ItemListener il = ev -> regen();
        gUp.addItemListener(il); gLo.addItemListener(il); gDg.addItemListener(il);
        gSy.addItemListener(il); gAmb.addItemListener(il);

        GridBagConstraints cg = new GridBagConstraints();
        cg.gridx = 0; cg.gridy = 0; cg.weightx = 1; cg.weighty = 1;
        cg.fill = GridBagConstraints.HORIZONTAL; cg.anchor = GridBagConstraints.NORTH;
        add(card, cg);

        regen();
    }

    private void regen() {
        int len = genLen.getValue();
        boolean up = gUp.isSelected(), lo = gLo.isSelected(), dg = gDg.isSelected(),
            sy = gSy.isSelected(), amb = gAmb.isSelected();
        genOut.setText(genPassword(len, up, lo, dg, sy, amb));
        int pool = poolSize(up, lo, dg, sy, amb);
        double bits = len * (Math.log(pool) / Math.log(2));
        meter.setBits(bits);
        meterLabel.setText("ENTROPY: " + (int) bits + " BITS  //  " + strengthWord(bits));
        genLenVal.setText(String.valueOf(len));
    }

    public static String genPassword(int len, boolean up, boolean lo, boolean dg, boolean sy, boolean noAmb) {
        String U = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String L = "abcdefghijklmnopqrstuvwxyz";
        String D = "0123456789";
        String S = "!@#$%^&*()-_=+[]{};:,.?/<>~";
        if (noAmb) {
            U = U.replace("O", "").replace("I", "");
            L = L.replace("l", "");
            D = D.replace("0", "").replace("1", "");
        }
        List<String> pools = new ArrayList<>();
        if (up) pools.add(U);
        if (lo) pools.add(L);
        if (dg) pools.add(D);
        if (sy) pools.add(S);
        if (pools.isEmpty()) pools.add(L);
        StringBuilder all = new StringBuilder();
        for (String p : pools) all.append(p);
        SecureRandom r = new SecureRandom();
        char[] out = new char[len];
        int i = 0;
        for (String p : pools) if (i < len) out[i++] = p.charAt(r.nextInt(p.length()));
        while (i < len) out[i++] = all.charAt(r.nextInt(all.length()));
        for (int k = len - 1; k > 0; k--) {
            int j = r.nextInt(k + 1);
            char t = out[k]; out[k] = out[j]; out[j] = t;
        }
        return new String(out);
    }

    private static int poolSize(boolean up, boolean lo, boolean dg, boolean sy, boolean amb) {
        int n = 0;
        if (up) n += amb ? 24 : 26;
        if (lo) n += amb ? 25 : 26;
        if (dg) n += amb ? 8 : 10;
        if (sy) n += 27;
        return n == 0 ? 26 : n;
    }

    private static String strengthWord(double bits) {
        if (bits < 45) return "WEAK";
        if (bits < 70) return "FAIR";
        if (bits < 100) return "STRONG";
        return "ELITE";
    }
}
