package ui.panels;

import model.PasswordEntry;
import ui.components.CyberButton;
import main.CyberVault;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;

import static ui.UIUtils.*;
import static ui.theme.ThemeManager.*;

public class PasswordsPanel extends JPanel {
    private JTextField passSearch;
    private JScrollPane passScroll;
    private JPanel passTagBar;
    private boolean showFavPass = false;

    public PasswordsPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(empty(22, 26, 20, 22));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(sectionHeader("PASSWORD DATABASE", "// logins \u2022 emails \u2022 accounts", NEON_CYAN), BorderLayout.CENTER);

        CyberButton add = new CyberButton("+ NEW ENTRY", NEON_CYAN, true);
        add.setPreferredSize(new Dimension(150, 38));
        add.addActionListener(ev -> openPasswordDialog(null));

        CyberButton favPass = new CyberButton(showFavPass ? "\u2605 FAVORITES" : "\u2606 FAVORITES", NEON_YEL, false);
        favPass.addActionListener(ev -> {
            showFavPass = !showFavPass;
            favPass.setText(showFavPass ? "\u2605 FAVORITES" : "\u2606 FAVORITES");
            refreshPasswords();
        });

        JPanel addWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addWrap.setOpaque(false);
        addWrap.add(favPass);
        addWrap.add(add);
        head.add(addWrap, BorderLayout.EAST);

        passSearch = searchField("SEARCH ENTRIES\u2026");
        passSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { refreshPasswords(); }
        });

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(passSearch, BorderLayout.CENTER);

        passTagBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        passTagBar.setOpaque(false);

        JPanel northWrap = new JPanel(new BorderLayout(0, 8));
        northWrap.setOpaque(false);
        northWrap.add(top, BorderLayout.NORTH);
        northWrap.add(passTagBar, BorderLayout.CENTER);
        add(northWrap, BorderLayout.NORTH);

        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setOpaque(false);
        passScroll = cyberScroll(placeholder);
        add(passScroll, BorderLayout.CENTER);
    }

    public void refreshPasswords() {
        String q = queryOf(passSearch).toLowerCase();
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(BG);
        inner.setBorder(empty(4, 2, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        int row = 0;

        if (CyberVault.manager != null && CyberVault.manager.active != null && CyberVault.manager.active.data != null) {
            for (PasswordEntry e : CyberVault.manager.active.data.passwords) {
                if (showFavPass && !e.favorite) continue;
                if (!q.isEmpty() && !((e.title + " " + e.username + " " + e.url + " " + tagsStr(e.tags)).toLowerCase().contains(q))) continue;
                g.gridy = row++; g.insets = new Insets(0, 0, 12, 0);
                inner.add(buildPasswordCard(e), g);
            }
        }
        if (row == 0) {
            g.gridy = 0; g.insets = new Insets(30, 0, 0, 0);
            inner.add(emptyState(q.isEmpty() ? "NO RECORDS YET // CLICK [+ NEW ENTRY]"
                : "NO MATCH FOUND"), g);
        }
        g.gridy = row; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        JPanel fill = new JPanel(); fill.setOpaque(false);
        inner.add(fill, g);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setBackground(BG);
        wrap.add(inner, BorderLayout.CENTER);
        passScroll.getViewport().setView(wrap);
        passScroll.getViewport().setBackground(BG);
        passTagBar.removeAll();

        if (CyberVault.manager != null && CyberVault.manager.active != null && CyberVault.manager.active.data != null) {
            LinkedHashSet<String> allTags = new LinkedHashSet<>();
            for (PasswordEntry e2 : CyberVault.manager.active.data.passwords)
                if (e2.tags != null) allTags.addAll(e2.tags);
            for (String tg : allTags) {
                JButton tb = chip("#" + tg, NEON_PURP);
                tb.addActionListener(ev -> { passSearch.setText(tg); passSearch.setForeground(TXT); refreshPasswords(); });
                passTagBar.add(tb);
            }
        }
        passTagBar.revalidate(); passTagBar.repaint();
        passScroll.revalidate();
        CyberVault.updateStats();
    }

    private JPanel buildPasswordCard(PasswordEntry e) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createMatteBorder(0, 3, 0, 0, NEON_CYAN)),
            empty(14, 16, 12, 14)));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel ttl = new JPanel(new GridLayout(0, 1, 0, 2));
        ttl.setOpaque(false);
        ttl.add(label(e.title.toUpperCase(), pickMono(Font.BOLD, 14f), TXT));
        ttl.add(label("ADDED " + fmtDate(e.created), F_MONO_S, new Color(0x555C82)));
        head.add(ttl, BorderLayout.CENTER);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        JButton fav = chip(e.favorite ? "\u2605" : "\u2606", NEON_YEL);
        fav.addActionListener(ev -> { e.favorite = !e.favorite; CyberVault.saveVault(); refreshPasswords(); });
        JButton edit = chip("EDIT", NEON_CYAN);
        JButton del = chip("DEL", NEON_PINK);
        edit.addActionListener(ev -> openPasswordDialog(e));
        del.addActionListener(ev -> {
            if (confirmAction("DELETE \"" + e.title.toUpperCase() + "\" PERMANENTLY?")) {
                CyberVault.manager.active.data.passwords.remove(e);
                CyberVault.saveVault(); refreshPasswords();
            }
        });

        acts.add(fav); acts.add(edit); acts.add(del);
        head.add(acts, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(0, 1, 0, 7));
        body.setOpaque(false);
        body.setBorder(empty(12, 0, 10, 0));

        body.add(row("USER/MAIL", label(cut(e.username, 60), F_MONO, TXT), actsOf(copyChip(e.username))));

        JLabel pv = label(mask(e.password.length()), F_MONO, NEON_PINK);
        JButton show = chip("SHOW", NEON_YEL);
        JButton copyPw = copyChip(e.password);
        boolean[] vis = { false };
        show.addActionListener(ev -> {
            vis[0] = !vis[0];
            pv.setText(vis[0] ? cut(e.password, 60) : mask(e.password.length()));
            pv.setForeground(vis[0] ? NEON_GRN : NEON_PINK);
            show.setText(vis[0] ? "HIDE" : "SHOW");
        });
        body.add(row("PASSWORD", pv, actsOf(show, copyPw)));

        if (!e.url.isEmpty()) {
            JButton open = chip("OPEN", NEON_CYAN);
            open.addActionListener(ev -> openUrl(e.url));
            body.add(row("URL", label(cut(e.url, 60), F_MONO, NEON_CYAN), actsOf(open, copyChip(e.url))));
        }
        if (!e.notes.isEmpty())
            body.add(row("NOTES", label(cut(e.notes, 70), F_MONO, TXT_DIM), null));

        if (e.tags != null && !e.tags.isEmpty()) {
            JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            tagPanel.setOpaque(false);
            for (String tg : e.tags) {
                JButton tc = chip("#" + tg, NEON_PURP);
                tc.addActionListener(ev -> { passSearch.setText(tg); passSearch.setForeground(TXT); refreshPasswords(); });
                tagPanel.add(tc);
            }
            body.add(tagPanel);
        }

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void openPasswordDialog(PasswordEntry ex) {
        JDialog d = cyberDialog(ex == null ? "NEW PASSWORD ENTRY" : "EDIT ENTRY", NEON_CYAN);
        JTextField fTitle = field(), fUser = field(), fUrl = field();
        JPasswordField fPass = passField();
        JTextArea fNotes = area();
        JTextField fTags = field();

        if (ex != null) {
            fTitle.setText(ex.title); fUser.setText(ex.username);
            fPass.setText(ex.password); fUrl.setText(ex.url); fNotes.setText(ex.notes);
            fTags.setText(ex.tags == null ? "" : String.join(", ", ex.tags));
        }
        JLabel err = label(" ", F_MONO_S, NEON_PINK);

        JButton gen = chip("\u26A1 GEN", NEON_YEL);
        gen.addActionListener(ev -> { fPass.setText(GeneratorPanel.genPassword(18, true, true, true, true, false)); fPass.setEchoChar((char) 0); });
        JButton eye = chip("SHOW", NEON_PURP);
        boolean[] vis = { false };
        eye.addActionListener(ev -> { vis[0] = !vis[0]; fPass.setEchoChar(vis[0] ? (char) 0 : '\u2022'); eye.setText(vis[0] ? "HIDE" : "SHOW"); });

        JPanel passRow = new JPanel(new BorderLayout(8, 0));
        passRow.setBackground(BG_PANEL);
        JPanel passBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        passBtns.setOpaque(false); passBtns.add(gen); passBtns.add(eye);
        passRow.add(fPass, BorderLayout.CENTER);
        passRow.add(passBtns, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addFormRow(form, 0, "TITLE *", fTitle);
        addFormRow(form, 1, "USER / EMAIL *", fUser);
        addFormRow(form, 2, "PASSWORD *", passRow);
        addFormRow(form, 3, "URL", fUrl);
        JScrollPane ns = new JScrollPane(fNotes);
        ns.setPreferredSize(new Dimension(0, 70));
        ns.setBorder(BorderFactory.createLineBorder(LINE));
        styleScroll(ns); ns.getViewport().setBackground(BG_FIELD);
        addFormRow(form, 4, "TAGS", fTags);
        addFormRow(form, 5, "NOTES", ns);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(BG_PANEL);
        body.setBorder(empty(22, 24, 24, 24));
        body.add(form, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout(0, 10));
        foot.setOpaque(false);
        foot.add(err, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        CyberButton save = new CyberButton("SAVE ENTRY", NEON_CYAN, true);
        CyberButton cancel = new CyberButton("CANCEL", TXT_DIM, false);

        save.addActionListener(ev -> {
            String t = fTitle.getText().trim(), u = fUser.getText().trim(), pw = new String(fPass.getPassword());
            if (t.isEmpty() || u.isEmpty() || pw.isEmpty()) { err.setText("\u2715 TITLE, USER & PASSWORD REQUIRED"); return; }
            PasswordEntry ent = ex != null ? ex : new PasswordEntry();
            ent.title = t; ent.username = u; ent.password = pw;
            ent.url = fUrl.getText().trim(); ent.notes = fNotes.getText().trim();
            ent.tags = parseTags(fTags.getText());
            if (ex == null) CyberVault.manager.active.data.passwords.add(ent);
            if (CyberVault.saveVault()) { refreshPasswords(); d.dispose(); }
        });
        cancel.addActionListener(ev -> d.dispose());
        btns.add(save); btns.add(cancel);
        foot.add(btns, BorderLayout.SOUTH);
        body.add(foot, BorderLayout.SOUTH);

        d.add(body, BorderLayout.CENTER);
        d.setSize(560, 500);
        d.setLocationRelativeTo(null);
        escapeToClose(d);
        d.setVisible(true);
    }
}
