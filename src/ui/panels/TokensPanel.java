package ui.panels;

import model.TokenEntry;
import ui.components.CyberButton;
import main.CyberVault;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;

import static ui.UIUtils.*;
import static ui.theme.ThemeManager.*;

public class TokensPanel extends JPanel {
    private JTextField tokSearch;
    private JScrollPane tokScroll;
    private JPanel tokTagBar;
    private boolean showFavTok = false;

    public TokensPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(empty(22, 26, 20, 22));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(sectionHeader("API TOKENS", "// hugging face \u2022 openai \u2022 github \u2022 \u2026", NEON_PURP), BorderLayout.CENTER);

        CyberButton add = new CyberButton("+ NEW TOKEN", NEON_PURP, true);
        add.setPreferredSize(new Dimension(160, 38));
        add.addActionListener(ev -> openTokenDialog(null));

        CyberButton favTok = new CyberButton(showFavTok ? "\u2605 FAVORITES" : "\u2606 FAVORITES", NEON_YEL, false);
        favTok.addActionListener(ev -> {
            showFavTok = !showFavTok;
            favTok.setText(showFavTok ? "\u2605 FAVORITES" : "\u2606 FAVORITES");
            refreshTokens();
        });

        JPanel addWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addWrap.setOpaque(false);
        addWrap.add(favTok);
        addWrap.add(add);
        head.add(addWrap, BorderLayout.EAST);

        tokSearch = searchField("SEARCH TOKENS\u2026");
        tokSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { refreshTokens(); }
        });

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(tokSearch, BorderLayout.CENTER);

        tokTagBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tokTagBar.setOpaque(false);

        JPanel northWrap = new JPanel(new BorderLayout(0, 8));
        northWrap.setOpaque(false);
        northWrap.add(top, BorderLayout.NORTH);
        northWrap.add(tokTagBar, BorderLayout.CENTER);
        add(northWrap, BorderLayout.NORTH);

        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setOpaque(false);
        tokScroll = cyberScroll(placeholder);
        add(tokScroll, BorderLayout.CENTER);
    }

    public void refreshTokens() {
        String q = queryOf(tokSearch).toLowerCase();
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(BG);
        inner.setBorder(empty(4, 2, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        int row = 0;

        if (CyberVault.manager != null && CyberVault.manager.active != null && CyberVault.manager.active.data != null) {
            for (TokenEntry e : CyberVault.manager.active.data.tokens) {
                if (showFavTok && !e.favorite) continue;
                if (!q.isEmpty() && !((e.name + " " + e.notes + " " + tagsStr(e.tags)).toLowerCase().contains(q))) continue;
                g.gridy = row++; g.insets = new Insets(0, 0, 12, 0);
                inner.add(buildTokenCard(e), g);
            }
        }
        if (row == 0) {
            g.gridy = 0; g.insets = new Insets(30, 0, 0, 0);
            inner.add(emptyState(q.isEmpty() ? "NO TOKENS YET // CLICK [+ NEW TOKEN]"
                : "NO MATCH FOUND"), g);
        }
        g.gridy = row; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        JPanel fill = new JPanel(); fill.setOpaque(false);
        inner.add(fill, g);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setBackground(BG);
        wrap.add(inner, BorderLayout.CENTER);
        tokScroll.getViewport().setView(wrap);
        tokScroll.getViewport().setBackground(BG);
        tokTagBar.removeAll();

        if (CyberVault.manager != null && CyberVault.manager.active != null && CyberVault.manager.active.data != null) {
            LinkedHashSet<String> allTags = new LinkedHashSet<>();
            for (TokenEntry e2 : CyberVault.manager.active.data.tokens)
                if (e2.tags != null) allTags.addAll(e2.tags);
            for (String tg : allTags) {
                JButton tb = chip("#" + tg, NEON_PURP);
                tb.addActionListener(ev -> { tokSearch.setText(tg); tokSearch.setForeground(TXT); refreshTokens(); });
                tokTagBar.add(tb);
            }
        }
        tokTagBar.revalidate(); tokTagBar.repaint();
        tokScroll.revalidate();
        CyberVault.updateStats();
    }

    private JPanel buildTokenCard(TokenEntry e) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createMatteBorder(0, 3, 0, 0, NEON_PURP)),
            empty(14, 16, 12, 14)));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel ttl = new JPanel(new GridLayout(0, 1, 0, 2));
        ttl.setOpaque(false);
        ttl.add(label(e.name.toUpperCase(), pickMono(Font.BOLD, 14f), NEON_PURP));
        ttl.add(label("ADDED " + fmtDate(e.created), F_MONO_S, new Color(0x555C82)));
        head.add(ttl, BorderLayout.CENTER);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        JButton fav = chip(e.favorite ? "\u2605" : "\u2606", NEON_YEL);
        fav.addActionListener(ev -> { e.favorite = !e.favorite; CyberVault.saveVault(); refreshTokens(); });
        JButton edit = chip("EDIT", NEON_CYAN);
        JButton del = chip("DEL", NEON_PINK);
        edit.addActionListener(ev -> openTokenDialog(e));
        del.addActionListener(ev -> {
            if (confirmAction("DELETE TOKEN \"" + e.name.toUpperCase() + "\" PERMANENTLY?")) {
                CyberVault.manager.active.data.tokens.remove(e);
                CyberVault.saveVault(); refreshTokens();
            }
        });

        acts.add(fav); acts.add(edit); acts.add(del);
        head.add(acts, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(0, 1, 0, 7));
        body.setOpaque(false);
        body.setBorder(empty(12, 0, 10, 0));

        JLabel tv = label(mask(e.token.length()), F_MONO, NEON_PINK);
        JButton show = chip("SHOW", NEON_YEL);
        boolean[] vis = { false };
        show.addActionListener(ev -> {
            vis[0] = !vis[0];
            tv.setText(vis[0] ? cut(e.token, 60) : mask(e.token.length()));
            tv.setForeground(vis[0] ? NEON_GRN : NEON_PINK);
            show.setText(vis[0] ? "HIDE" : "SHOW");
        });
        body.add(row("TOKEN", tv, actsOf(show, copyChip(e.token))));
        if (!e.notes.isEmpty())
            body.add(row("NOTES", label(cut(e.notes, 70), F_MONO, TXT_DIM), null));

        if (e.tags != null && !e.tags.isEmpty()) {
            JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            tagPanel.setOpaque(false);
            for (String tg : e.tags) {
                JButton tc = chip("#" + tg, NEON_PURP);
                tc.addActionListener(ev -> { tokSearch.setText(tg); tokSearch.setForeground(TXT); refreshTokens(); });
                tagPanel.add(tc);
            }
            body.add(tagPanel);
        }

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void openTokenDialog(TokenEntry ex) {
        JDialog d = cyberDialog(ex == null ? "NEW API TOKEN" : "EDIT API TOKEN", NEON_PURP);
        JTextField fName = field();
        JPasswordField fTok = passField();
        JTextArea fNotes = area();
        JTextField fTags = field();

        if (ex != null) {
            fName.setText(ex.name); fTok.setText(ex.token); fNotes.setText(ex.notes);
            fTags.setText(ex.tags == null ? "" : String.join(", ", ex.tags));
        }
        JLabel err = label(" ", F_MONO_S, NEON_PINK);

        JButton show = chip("SHOW", NEON_YEL);
        boolean[] vis = { false };
        show.addActionListener(ev -> { vis[0] = !vis[0]; fTok.setEchoChar(vis[0] ? (char) 0 : '\u2022'); show.setText(vis[0] ? "HIDE" : "SHOW"); });

        JPanel tokRow = new JPanel(new BorderLayout(8, 0));
        tokRow.setBackground(BG_PANEL);
        tokRow.add(fTok, BorderLayout.CENTER);
        tokRow.add(show, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addFormRow(form, 0, "PROVIDER *", fName);
        addFormRow(form, 1, "TOKEN *", tokRow);
        JScrollPane ns = new JScrollPane(fNotes);
        ns.setPreferredSize(new Dimension(0, 74));
        ns.setBorder(BorderFactory.createLineBorder(LINE));
        styleScroll(ns); ns.getViewport().setBackground(BG_FIELD);
        addFormRow(form, 2, "TAGS", fTags);
        addFormRow(form, 3, "NOTES", ns);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(22, 24, 24, 24));
        body.add(form, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout(0, 10));
        foot.setOpaque(false);
        foot.add(err, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        CyberButton save = new CyberButton("SAVE TOKEN", NEON_PURP, true);
        CyberButton cancel = new CyberButton("CANCEL", TXT_DIM, false);

        save.addActionListener(ev -> {
            String n = fName.getText().trim();
            String t = new String(fTok.getPassword()).trim();
            if (n.isEmpty() || t.isEmpty()) { err.setText("\u2715 PROVIDER & TOKEN ARE REQUIRED"); return; }
            TokenEntry ent = ex != null ? ex : new TokenEntry();
            ent.name = n; ent.token = t; ent.notes = fNotes.getText().trim();
            ent.tags = parseTags(fTags.getText());
            if (ex == null) CyberVault.manager.active.data.tokens.add(ent);
            if (CyberVault.saveVault()) { refreshTokens(); d.dispose(); }
        });
        cancel.addActionListener(ev -> d.dispose());
        btns.add(save); btns.add(cancel);
        foot.add(btns, BorderLayout.SOUTH);
        body.add(foot, BorderLayout.SOUTH);

        d.add(body, BorderLayout.CENTER);
        d.setSize(520, 420);
        d.setLocationRelativeTo(null);
        escapeToClose(d);
        d.setVisible(true);
    }
}
