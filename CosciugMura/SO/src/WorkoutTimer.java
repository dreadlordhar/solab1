import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Workout Timer - interfata dark (inel de progres, meniu lateral, carduri) si 3 timere:
 *  1. reminderTimer - schedule(task, Date)                  -> notificare la ora aleasa: "Incepe antrenamentul!"
 *  2. clockTimer    - scheduleAtFixedRate(task, 1000, 1000) -> porneste la START, numaratoarea de o secunda
 *  3. waterTimer    - schedule(task, delay, period)         -> la fiecare 5 minute: "Hidrateaza-te!"
 * Toata logica ruleaza pe thread-ul Swing (invokeLater).
 * Compilare: javac -encoding UTF-8 WorkoutTimer.java
 */
public class WorkoutTimer extends JFrame {

    // ---------- Stil ----------
    static final Color BG = new Color(9, 18, 34), SIDE = new Color(12, 24, 45), CARD = new Color(15, 29, 52),
            CARD2 = new Color(24, 42, 72), ACCENT = new Color(20, 110, 245), TEXT = new Color(240, 245, 255),
            MUTED = new Color(140, 158, 188), TRACK = new Color(22, 38, 68), GOLD = new Color(255, 226, 140);

    static Font f(int style, float size) { return new Font("Segoe UI", style, Math.round(size)); }

    static JLabel lbl(String t, int style, float size, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(f(style, size));
        l.setForeground(c);
        return l;
    }

    static Graphics2D aa(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    /** Icoane desenate cu Graphics2D (centrate in cx, cy; s = dimensiunea). */
    static void icon(Graphics2D g, String k, int cx, int cy, int s, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(Math.max(2f, s / 9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int h = s / 2;
        switch (k) {
            case "home":
                g.fillPolygon(new int[]{cx - h, cx, cx + h}, new int[]{cy, cy - h, cy}, 3);
                g.fillRect(cx - h * 2 / 3, cy, h * 4 / 3, h);
                break;
            case "dumb":
                g.fillRoundRect(cx - h, cy - h * 2 / 3, s / 5, h * 4 / 3, 3, 3);
                g.fillRoundRect(cx + h - s / 5, cy - h * 2 / 3, s / 5, h * 4 / 3, 3, 3);
                g.fillRect(cx - h, cy - s / 14, s, s / 7);
                break;
            case "clock":
                g.drawOval(cx - h, cy - h, s, s);
                g.drawLine(cx, cy, cx, cy - h * 2 / 3);
                g.drawLine(cx, cy, cx + h / 2, cy);
                break;
            case "gear":
                g.drawOval(cx - h / 2, cy - h / 2, h, h);
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    g.drawLine((int) (cx + Math.cos(a) * h * 0.7), (int) (cy + Math.sin(a) * h * 0.7),
                            (int) (cx + Math.cos(a) * h), (int) (cy + Math.sin(a) * h));
                }
                break;
            case "play":
                g.fillPolygon(new int[]{cx - h / 2, cx - h / 2, cx + h * 2 / 3}, new int[]{cy - h, cy + h, cy}, 3);
                break;
            case "pause":
                g.fillRoundRect(cx - h * 2 / 3, cy - h, h / 2 + 2, s, 3, 3);
                g.fillRoundRect(cx + h / 6 + 1, cy - h, h / 2 + 2, s, 3, 3);
                break;
            case "prev":
                g.fillRect(cx - h, cy - h * 3 / 4, 3, h * 3 / 2);
                g.fillPolygon(new int[]{cx + h, cx + h, cx - h / 2}, new int[]{cy - h * 3 / 4, cy + h * 3 / 4, cy}, 3);
                break;
            case "next":
                g.fillRect(cx + h - 3, cy - h * 3 / 4, 3, h * 3 / 2);
                g.fillPolygon(new int[]{cx - h, cx - h, cx + h / 2}, new int[]{cy - h * 3 / 4, cy + h * 3 / 4, cy}, 3);
                break;
            case "pencil":
                g.drawLine(cx - h, cy + h, cx + h, cy - h);
                g.drawLine(cx - h, cy + h, cx - h + 4, cy + h);
                break;
            case "stop":
                g.fillRoundRect(cx - h * 2 / 3, cy - h * 2 / 3, h * 4 / 3, h * 4 / 3, 3, 3);
                break;
            case "figure":
                g.fillOval(cx - s / 8, cy - h, s / 4, s / 4);
                g.drawLine(cx, cy - h + s / 4, cx, cy + h / 4);
                g.drawLine(cx, cy - h / 4, cx - h, cy - h * 3 / 5);
                g.drawLine(cx, cy - h / 4, cx + h, cy - h * 3 / 5);
                g.drawLine(cx, cy + h / 4, cx - h * 3 / 5, cy + h);
                g.drawLine(cx, cy + h / 4, cx + h * 3 / 5, cy + h);
                break;
            case "cup":
                g.drawRoundRect(cx - h * 2 / 3, cy - h / 3, h, h + h / 3, 4, 4);
                g.drawArc(cx + h / 6, cy - h / 6, h / 2, h / 2, -90, 180);
                break;
            case "trend":
                g.drawPolyline(new int[]{cx - h, cx - h / 3, cx, cx + h}, new int[]{cy + h / 2, cy, cy + h / 4, cy - h / 2}, 4);
                g.drawLine(cx + h, cy - h / 2, cx + h - 5, cy - h / 2);
                g.drawLine(cx + h, cy - h / 2, cx + h, cy - h / 2 + 5);
                break;
            default:
        }
    }

    // ---------- Componente grafice ----------
    static class Card extends JPanel {
        Card() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            g.setColor(CARD);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 22, 22));
            g.setColor(TRACK);
            g.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 22, 22));
            g.dispose();
        }
    }

    static class Round extends JButton {
        String kind;
        Color bg;
        Round(String kind, Color bg, int size) {
            this.kind = kind; this.bg = bg;
            setPreferredSize(new Dimension(size, size));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            g.setColor(bg);
            g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            icon(g, kind, getWidth() / 2, getHeight() / 2, getWidth() * 2 / 5, TEXT);
            g.dispose();
        }
    }

    static class Pill extends JButton {
        Pill(String t) {
            super(t);
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setForeground(TEXT); setFont(f(Font.BOLD, 14)); setBorder(new EmptyBorder(9, 20, 9, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            g.setColor(ACCENT);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static class Nav extends JButton {
        final String kind, page;
        boolean sel;
        Nav(String text, String kind, String page) {
            super(text);
            this.kind = kind; this.page = page;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setAlignmentX(0f);
            setPreferredSize(new Dimension(172, 42)); setMaximumSize(new Dimension(172, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            if (sel) { g.setColor(ACCENT); g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14); }
            icon(g, kind, 26, getHeight() / 2, 18, sel ? TEXT : MUTED);
            g.setFont(f(Font.BOLD, 15));
            g.setColor(sel ? TEXT : MUTED);
            g.drawString(getText(), 50, getHeight() / 2 + 5);
            g.dispose();
        }
    }

    static class Ic extends JComponent {
        String kind; Color c, bg;
        Ic(String kind, Color c, int size, Color bg) {
            this.kind = kind; this.c = c; this.bg = bg;
            setPreferredSize(new Dimension(size, size));
        }
        void set(String kind, Color bg) { this.kind = kind; this.bg = bg; repaint(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            int w = getWidth(), h = getHeight();
            if (bg != null) { g.setColor(bg); g.fillOval(0, 0, w - 1, h - 1); }
            icon(g, kind, w / 2, h / 2, (int) (Math.min(w, h) * (bg != null ? 0.5 : 0.8)), c);
            g.dispose();
        }
    }

    static class Bar extends JComponent {
        double value;
        Bar() { setPreferredSize(new Dimension(200, 10)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 10)); setAlignmentX(0f); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            g.setColor(TRACK);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g.setColor(ACCENT);
            g.fillRoundRect(0, 0, (int) (getWidth() * Math.max(0, Math.min(1, value))), getHeight(), 10, 10);
            g.dispose();
        }
    }

    // ---------- Model ----------
    enum Mode { IDLE, RUN, DONE }

    static class Step {
        final boolean work; final String name; final int sec, round;
        Step(boolean work, String name, int sec, int round) { this.work = work; this.name = name; this.sec = sec; this.round = round; }
    }

    final List<Step> steps = new ArrayList<>();
    Mode mode = Mode.IDLE;
    boolean paused;
    int si, left, total, elapsed, workSec, restSec, rounds;
    String name = "Full Body";
    Timer reminderTimer, clockTimer, waterTimer;
    TrayIcon tray;
    javax.swing.Timer hideTimer;   // Timer Swing, doar pentru ascunderea bannerului

    // ---------- Componente UI ----------
    final CardLayout cards = new CardLayout();
    final JPanel pages = new JPanel(cards);
    final List<Nav> navs = new ArrayList<>();
    final JLabel titleLbl = lbl("", Font.BOLD, 20, TEXT);
    final JLabel statRounds = lbl("", Font.BOLD, 17, TEXT), statWork = lbl("", Font.BOLD, 17, TEXT), statRest = lbl("", Font.BOLD, 17, TEXT);
    final JLabel noticeLbl = lbl(" ", Font.BOLD, 16, GOLD);
    final JLabel curName = lbl("", Font.BOLD, 15, TEXT), curRound = lbl("", Font.PLAIN, 13, MUTED);
    final JLabel nextName = lbl("", Font.BOLD, 15, TEXT), nextSub = lbl("", Font.PLAIN, 13, MUTED);
    final Ic curIcon = new Ic("figure", TEXT, 46, ACCENT), nextIcon = new Ic("cup", TEXT, 46, CARD2);
    final Bar curBar = new Bar();
    final Ring ring = new Ring();
    final Round playBtn = new Round("play", ACCENT, 76);
    final JTextArea log = new JTextArea();

    final JTextField tfName = new JTextField("Full Body");
    final JTextArea taEx = new JTextArea("Jumping Jacks\nSquats\nPush-ups", 4, 20);
    final JSpinner sWork = new JSpinner(new SpinnerNumberModel(30, 1, 600, 1));
    final JSpinner sRest = new JSpinner(new SpinnerNumberModel(15, 1, 600, 1));
    final JSpinner sRounds = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
    final JSpinner sTime = new JSpinner(new SpinnerDateModel(new Date(System.currentTimeMillis() + 30000), null, null, Calendar.SECOND));
    final JSpinner sWater = new JSpinner(new SpinnerNumberModel(300, 5, 3600, 5));   // 300 s = 5 minute

    public WorkoutTimer() {
        super("Workout Timer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initTray();
        getContentPane().setBackground(BG);

        // --- Sidebar ---
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(SIDE);
        side.setBorder(new EmptyBorder(24, 14, 24, 14));
        side.setPreferredSize(new Dimension(200, 0));
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logo.setOpaque(false); logo.setAlignmentX(0f); logo.setMaximumSize(new Dimension(172, 50));
        logo.add(new Ic("dumb", ACCENT, 34, null));
        logo.add(lbl("Workout Timer", Font.BOLD, 15, TEXT));
        side.add(logo);
        side.add(Box.createVerticalStrut(24));
        String[][] items = {{"Acasă", "home", "home"}, {"Antrenamente", "dumb", "cfg"}, {"Istoric", "clock", "hist"}, {"Setări", "gear", "set"}};
        for (String[] it : items) {
            Nav n = new Nav(it[0], it[1], it[2]);
            n.addActionListener(e -> show(n.page));
            navs.add(n);
            side.add(n);
            side.add(Box.createVerticalStrut(6));
        }
        side.add(Box.createVerticalGlue());
        JPanel quote = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        quote.setOpaque(false); quote.setAlignmentX(0f); quote.setMaximumSize(new Dimension(172, 90));
        quote.add(new Ic("trend", ACCENT, 26, null));
        JLabel q = lbl("<html><div style='width:150px'>„Disciplina este puntea dintre obiective și realizări.”</div></html>", Font.PLAIN, 12, MUTED);
        JPanel qWrap = new JPanel(new BorderLayout());
        qWrap.setOpaque(false); qWrap.setAlignmentX(0f); qWrap.setMaximumSize(new Dimension(172, 90));
        qWrap.add(quote, BorderLayout.NORTH); qWrap.add(q, BorderLayout.CENTER);
        side.add(qWrap);

        // --- Pagini ---
        pages.setOpaque(false);
        pages.add(buildHome(), "home");
        pages.add(buildConfig(), "cfg");
        pages.add(buildHistory(), "hist");
        pages.add(buildSettings(), "set");

        add(side, BorderLayout.WEST);
        add(pages, BorderLayout.CENTER);

        reset();
        show("home");
        setSize(1020, 660);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    // ---------- Constructie pagini ----------
    JPanel buildHome() {
        JPanel home = new JPanel(new BorderLayout(20, 0));
        home.setOpaque(false);
        home.setBorder(new EmptyBorder(24, 24, 24, 24));

        Round edit = new Round("pencil", TRACK, 30), stop = new Round("stop", TRACK, 30);
        edit.addActionListener(e -> show("cfg"));
        stop.addActionListener(e -> reset());
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        titleRow.setOpaque(false);
        titleRow.add(titleLbl); titleRow.add(edit); titleRow.add(stop);
        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.add(stat("Runde", statRounds)); stats.add(stat("Activitate", statWork)); stats.add(stat("Pauză", statRest));
        JPanel header = new JPanel(new BorderLayout(0, 14));
        header.setOpaque(false);
        header.add(titleRow, BorderLayout.NORTH); header.add(stats, BorderLayout.CENTER);

        Round prev = new Round("prev", CARD2, 54), next = new Round("next", CARD2, 54);
        prev.addActionListener(e -> goTo(si - 1));
        next.addActionListener(e -> { if (mode == Mode.RUN) goTo(si + 1); });
        playBtn.addActionListener(e -> togglePlay());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
        controls.setOpaque(false);
        controls.add(prev); controls.add(playBtn); controls.add(next);
        noticeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(controls, BorderLayout.CENTER); bottom.add(noticeLbl, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(header, BorderLayout.NORTH); center.add(ring, BorderLayout.CENTER); center.add(bottom, BorderLayout.SOUTH);

        // Carduri dreapta
        Card cur = new Card(), nxt = new Card();
        for (Card c : new Card[]{cur, nxt}) {
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
            c.setBorder(new EmptyBorder(16, 18, 16, 18));
            c.setMaximumSize(new Dimension(270, 175));
            c.setAlignmentX(0f);
        }
        cur.add(left(lbl("Exercițiul curent", Font.BOLD, 15, TEXT)));
        cur.add(Box.createVerticalStrut(14));
        cur.add(left(iconRow(curIcon, curName)));
        cur.add(Box.createVerticalStrut(14));
        cur.add(curBar);
        cur.add(Box.createVerticalStrut(8));
        cur.add(left(curRound));
        JPanel two = new JPanel(new GridLayout(2, 1));
        two.setOpaque(false);
        two.add(nextName); two.add(nextSub);
        nxt.add(left(lbl("Următorul", Font.BOLD, 15, TEXT)));
        nxt.add(Box.createVerticalStrut(14));
        nxt.add(left(iconRow(nextIcon, two)));

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(270, 0));
        right.add(Box.createVerticalStrut(10));
        right.add(cur); right.add(Box.createVerticalStrut(16)); right.add(nxt);
        right.add(Box.createVerticalGlue());

        home.add(center, BorderLayout.CENTER);
        home.add(right, BorderLayout.EAST);
        return home;
    }

    JPanel stat(String cap, JLabel val) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setOpaque(false);
        JLabel c = lbl(cap, Font.PLAIN, 13, MUTED);
        c.setHorizontalAlignment(SwingConstants.CENTER);
        val.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(c); p.add(val);
        return p;
    }

    JPanel iconRow(JComponent icon, JComponent text) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        r.setOpaque(false);
        r.add(icon); r.add(text);
        return r;
    }

    static JComponent left(JComponent c) { c.setAlignmentX(0f); return c; }

    JPanel buildConfig() {
        JPanel form = form();
        dark(tfName);
        taEx.setLineWrap(true);
        dark(taEx);
        darkSpin(sWork); darkSpin(sRest); darkSpin(sRounds);
        JButton save = new Pill("Salvează și resetează");
        save.addActionListener(e -> { reset(); show("home"); });
        row(form, 0, "Nume antrenament", tfName);
        row(form, 1, "Exerciții (unul pe linie)", new JScrollPane(taEx));
        row(form, 2, "Activitate (s)", sWork);
        row(form, 3, "Pauză (s)", sRest);
        row(form, 4, "Runde", sRounds);
        row(form, 5, "", save);
        return wrap("Antrenamente", form);
    }

    JPanel buildSettings() {
        JPanel form = form();
        sTime.setEditor(new JSpinner.DateEditor(sTime, "HH:mm:ss"));
        darkSpin(sTime); darkSpin(sWater);
        JButton remind = new Pill("Programează reminder");
        remind.addActionListener(e -> { scheduleReminder(); show("hist"); });
        row(form, 0, "Reminder start la ora", sTime);
        row(form, 1, "", remind);
        row(form, 2, "Hidratare la fiecare (s)", sWater);
        return wrap("Setări", form);
    }

    JPanel buildHistory() {
        log.setEditable(false);
        log.setBackground(CARD); log.setForeground(TEXT); log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        log.setBorder(new EmptyBorder(12, 12, 12, 12));
        JScrollPane sp = new JScrollPane(log);
        sp.setBorder(BorderFactory.createLineBorder(TRACK));
        sp.getViewport().setBackground(CARD);
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        p.add(lbl("Istoric", Font.BOLD, 22, TEXT), BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    JPanel form() {
        Card c = new Card();
        c.setLayout(new GridBagLayout());
        c.setBorder(new EmptyBorder(20, 20, 20, 20));
        return c;
    }

    void row(JPanel p, int y, String label, JComponent comp) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y; g.insets = new Insets(8, 8, 8, 8); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; p.add(lbl(label, Font.PLAIN, 14, MUTED), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(comp, g);
    }

    JPanel wrap(String title, JPanel form) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(form, BorderLayout.NORTH);
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        p.add(lbl(title, Font.BOLD, 22, TEXT), BorderLayout.NORTH);
        p.add(holder, BorderLayout.CENTER);
        return p;
    }

    static void dark(JComponent c) {
        c.setBackground(CARD2); c.setForeground(TEXT); c.setFont(f(Font.PLAIN, 14));
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TRACK), new EmptyBorder(4, 8, 4, 8)));
        if (c instanceof JTextComponent) ((JTextComponent) c).setCaretColor(TEXT);
    }

    static void darkSpin(JSpinner s) {
        JTextField t = ((JSpinner.DefaultEditor) s.getEditor()).getTextField();
        t.setBackground(CARD2); t.setForeground(TEXT); t.setCaretColor(TEXT); t.setFont(f(Font.PLAIN, 14));
        s.setBorder(BorderFactory.createLineBorder(TRACK));
        s.setPreferredSize(new Dimension(130, 32));
    }

    void show(String page) {
        cards.show(pages, page);
        for (Nav n : navs) { n.sel = n.page.equals(page); n.repaint(); }
    }

    // ---------- Inelul de progres ----------
    class Ring extends JPanel {
        Ring() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            int w = getWidth(), h = getHeight();
            int size = Math.min(w, h) - 20, th = Math.max(12, size / 24);
            int x = (w - size) / 2, y = (h - size) / 2;
            double d = size - th;
            double frac = mode == Mode.IDLE ? 1 : (total > 0 ? (double) left / total : 0);
            g.setStroke(new BasicStroke(th, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(TRACK);
            g.draw(new Ellipse2D.Double(x + th / 2.0, y + th / 2.0, d, d));
            g.setColor(ACCENT);
            if (frac > 0) g.draw(new Arc2D.Double(x + th / 2.0, y + th / 2.0, d, d, 90, -360 * frac, Arc2D.OPEN));

            Step s = steps.get(Math.min(si, steps.size() - 1));
            String top = mode == Mode.IDLE ? "Pregătit" : mode == Mode.DONE ? "Terminat" : paused ? "În pauză" : s.work ? "Activitate" : "Pauză";
            g.setColor(TEXT);
            g.setFont(f(Font.PLAIN, size / 14f));
            centered(g, top, w / 2, h / 2 - size / 6);
            g.setFont(f(Font.BOLD, size / 4f));
            centered(g, fmt(left), w / 2, h / 2 + size / 12);
            g.setColor(MUTED);
            g.setFont(f(Font.PLAIN, size / 17f));
            centered(g, "Rundă " + s.round + "/" + rounds, w / 2, h / 2 + size / 4);
            g.dispose();
        }

        void centered(Graphics2D g, String s, int cx, int baseline) {
            g.drawString(s, cx - g.getFontMetrics().stringWidth(s) / 2, baseline);
        }
    }

    // ---------- Timerele ----------

    /** TIMER 1 - MOMENT ANUME: schedule(task, Date) - notificare "incepe antrenamentul". */
    void scheduleReminder() {
        if (mode == Mode.RUN) return;
        if (reminderTimer != null) reminderTimer.cancel();
        Calendar chosen = Calendar.getInstance();
        chosen.setTime((Date) sTime.getValue());
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, chosen.get(Calendar.HOUR_OF_DAY));
        target.set(Calendar.MINUTE, chosen.get(Calendar.MINUTE));
        target.set(Calendar.SECOND, chosen.get(Calendar.SECOND));
        target.set(Calendar.MILLISECOND, 0);
        if (target.getTimeInMillis() <= System.currentTimeMillis()) target.add(Calendar.DAY_OF_MONTH, 1);
        Date when = target.getTime();
        reminderTimer = new Timer(true);
        reminderTimer.schedule(new TimerTask() {
            @Override public void run() { SwingUtilities.invokeLater(WorkoutTimer.this::remindStart); }
        }, when);
        log(String.format("Reminder programat la %tT", when));
    }

    void remindStart() {
        notifyUser("Workout Timer", "E timpul să începi antrenamentul!");
        toFront();
        JOptionPane.showMessageDialog(this, "E timpul să începi antrenamentul!\nApasă Play când ești gata.",
                "Reminder", JOptionPane.INFORMATION_MESSAGE);
    }

    void startWorkout() {
        if (mode != Mode.IDLE) reset();
        if (reminderTimer != null) reminderTimer.cancel();
        mode = Mode.RUN;
        paused = false;
        log("Start: " + name + " (" + rounds + " runde)");
        refreshUI();
        startClock();
        startWater();
    }

    /** TIMER 2 - PERIODIC (rata fixa): scheduleAtFixedRate(task, 1000, 1000) - porneste la inceputul antrenamentului. */
    void startClock() {
        clockTimer = new Timer(true);
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { SwingUtilities.invokeLater(WorkoutTimer.this::tick); }
        }, 1000, 1000);
    }

    /** TIMER 3 - PERIODIC (intarziere fixa): schedule(task, delay, period) - hidratare la fiecare 5 minute. */
    void startWater() {
        long ms = 1000L * ((Integer) sWater.getValue());
        waterTimer = new Timer(true);
        waterTimer.schedule(new TimerTask() {
            @Override public void run() { SwingUtilities.invokeLater(WorkoutTimer.this::remindWater); }
        }, ms, ms);
    }

    void remindWater() {
        if (mode != Mode.RUN) return;
        notifyUser("Hidratare", "Bea apă! (" + fmt(elapsed) + " de antrenament)");
    }

    void togglePlay() {
        if (mode == Mode.RUN) togglePause();
        else startWorkout();
    }

    void togglePause() {
        if (!paused) {
            clockTimer.cancel();          // dupa cancel(), un Timer nu mai poate fi refolosit
            waterTimer.cancel();
            paused = true;
            log("Pauză");
        } else {
            startClock();                 // deci cream Timere noi
            startWater();
            paused = false;
            log("Reluare");
        }
        refreshUI();
    }

    // ---------- Logica antrenamentului ----------
    void tick() {
        if (mode != Mode.RUN) return;
        elapsed++;
        left--;
        if (left > 0 && left <= 3) Toolkit.getDefaultToolkit().beep();   // numaratoare finala
        if (left <= 0) {
            Toolkit.getDefaultToolkit().beep();
            goTo(si + 1);
            return;
        }
        refreshUI();
    }

    void goTo(int idx) {
        if (mode != Mode.RUN) return;
        if (idx >= steps.size()) { finish(); return; }
        si = Math.max(0, idx);
        Step s = steps.get(si);
        left = total = s.sec;
        log("Runda " + s.round + "/" + rounds + ": " + (s.work ? s.name : "Pauză"));
        refreshUI();
    }

    void finish() {
        mode = Mode.DONE;
        cancelTimers();
        left = 0;
        log("Antrenament terminat! Timp total: " + fmt(elapsed));
        notifyUser("Workout Timer", "Antrenament terminat, bravo!");
    }

    void reset() {
        cancelTimers();
        mode = Mode.IDLE;
        paused = false;
        name = tfName.getText().trim().isEmpty() ? "Antrenament" : tfName.getText().trim();
        workSec = (Integer) sWork.getValue();
        restSec = (Integer) sRest.getValue();
        rounds = (Integer) sRounds.getValue();
        String[] ex = Arrays.stream(taEx.getText().split("\\n")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        if (ex.length == 0) ex = new String[]{"Exercițiu"};
        steps.clear();
        for (int r = 1; r <= rounds; r++)
            for (int i = 0; i < ex.length; i++) {
                steps.add(new Step(true, ex[i], workSec, r));
                if (!(r == rounds && i == ex.length - 1)) steps.add(new Step(false, "Pauză", restSec, r));
            }
        si = 0; elapsed = 0;
        left = total = steps.get(0).sec;
        noticeLbl.setText(" ");
        refreshUI();
    }

    void cancelTimers() {
        if (reminderTimer != null) reminderTimer.cancel();
        if (clockTimer != null) clockTimer.cancel();
        if (waterTimer != null) waterTimer.cancel();
    }

    // ---------- Notificari ----------
    void initTray() {
        if (!SystemTray.isSupported()) return;
        try {
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(ACCENT);
            g.fillOval(0, 0, 15, 15);
            g.dispose();
            tray = new TrayIcon(img, "Workout Timer");
            SystemTray.getSystemTray().add(tray);
        } catch (Exception ex) {
            tray = null;
        }
    }

    /** Notificare de sistem (daca e suportata) + bip + banner in fereastra + istoric. */
    void notifyUser(String title, String msg) {
        Toolkit.getDefaultToolkit().beep();
        if (tray != null) tray.displayMessage(title, msg, TrayIcon.MessageType.INFO);
        noticeLbl.setText(msg);
        log(title + ": " + msg);
        if (hideTimer != null) hideTimer.stop();
        hideTimer = new javax.swing.Timer(6000, e -> noticeLbl.setText(" "));
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    // ---------- Helpers ----------
    void refreshUI() {
        titleLbl.setText("Antrenament: " + name);
        statRounds.setText(String.valueOf(rounds));
        statWork.setText(workSec + "s");
        statRest.setText(restSec + "s");
        Step s = steps.get(Math.min(si, steps.size() - 1));
        curIcon.set(s.work ? "figure" : "cup", s.work ? ACCENT : CARD2);
        curName.setText(mode == Mode.DONE ? "Terminat" : s.name);
        curBar.value = mode == Mode.IDLE ? 0 : (total > 0 ? (double) (total - left) / total : 0);
        curBar.repaint();
        curRound.setText("Rundă " + s.round + "/" + rounds);
        Step n = si + 1 < steps.size() ? steps.get(si + 1) : null;
        nextIcon.set(n == null || !n.work ? "cup" : "figure", n != null && n.work ? ACCENT : CARD2);
        nextName.setText(n == null ? "Final" : n.name);
        nextSub.setText(n == null ? "Antrenament încheiat" : n.sec + " secunde");
        playBtn.kind = mode == Mode.RUN && !paused ? "pause" : "play";
        playBtn.repaint();
        ring.repaint();
    }

    static String fmt(int s) { return String.format("%02d:%02d", s / 60, s % 60); }

    void log(String s) {
        log.append(String.format("[%tT] %s%n", new Date(), s));
        log.setCaretPosition(log.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WorkoutTimer().setVisible(true));
    }
}