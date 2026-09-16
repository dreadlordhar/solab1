import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Lucrare de laborator 1 - Mecanism de planificare a activitatii proceselor
 * utilizand clasele Timer si TimerTask.
 *
 * Contine 3 timere, pornite manual printr-un buton "Start":
 *  1. Timer cu rata fixa (scheduleAtFixedRate) - contor la fiecare 1 secunda
 *  2. Timer cu executie unica dupa o intarziere (schedule cu delay)
 *  3. Timer programat la un moment exact (schedule cu Date) - afiseaza o
 *     notificare de sistem (System Tray) cand se declanseaza
 */
public class TimerApp extends JFrame {

    // Paleta de culori
    private static final Color BG_DARK = new Color(24, 26, 32);
    private static final Color CARD_BG = new Color(34, 37, 46);
    private static final Color ACCENT_1 = new Color(88, 166, 255);   // albastru
    private static final Color ACCENT_2 = new Color(255, 159, 67);   // portocaliu
    private static final Color ACCENT_3 = new Color(87, 214, 141);   // verde
    private static final Color TEXT_MAIN = new Color(230, 233, 240);
    private static final Color TEXT_DIM = new Color(150, 156, 168);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_STATUS = new Font("Segoe UI", Font.PLAIN, 12);
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss");

    private final JLabel lblCounterValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblCounterStatus = new JLabel("În așteptare...", SwingConstants.CENTER);
    private final JProgressBar barCounter = new JProgressBar(0, 100);

    private final JLabel lblDelayValue = new JLabel("5", SwingConstants.CENTER);
    private final JLabel lblDelayStatus = new JLabel("În așteptare...", SwingConstants.CENTER);
    private final JProgressBar barDelay = new JProgressBar(0, 5);

    private final JLabel lblClockValue = new JLabel("--:--:--", SwingConstants.CENTER);
    private final JLabel lblClockStatus = new JLabel("În așteptare...", SwingConstants.CENTER);

    private final JTextArea log = new JTextArea();
    private final JButton btnStart = new JButton("\u25B6  Start timere");
    private final JButton btnStop = new JButton("\u23F9  Oprește toate timerele");

    private Timer timerInterval;
    private Timer timerDelay;
    private Timer timerClock;
    private Timer uiTicker;

    private int counter = 0;
    private int delayRemaining = 5;
    private Date oraTinta;
    private TrayIcon trayIcon;

    public TimerApp() {
        super("Planificarea proceselor \u2014 Timer & TimerTask");
        buildUI();
        setupTray();
    }

    /* ================= INTERFATA ================= */
    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        JLabel header = new JLabel("Sisteme de Operare \u00b7 Laborator 1", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(TEXT_MAIN);
        header.setBorder(new EmptyBorder(18, 10, 8, 10));
        header.setOpaque(true);
        header.setBackground(BG_DARK);
        add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setBackground(BG_DARK);
        cards.setBorder(new EmptyBorder(6, 16, 16, 16));

        cards.add(buildCard("TIMER 1 \u2014 Rată fixă", "Tick la fiecare 1 secundă",
                lblCounterValue, lblCounterStatus, barCounter, ACCENT_1));
        cards.add(buildCard("TIMER 2 \u2014 Întârziere", "Execuție unică după 5 secunde",
                lblDelayValue, lblDelayStatus, barDelay, ACCENT_2));
        cards.add(buildCard("TIMER 3 \u2014 Oră exactă", "Notificare la un moment fix",
                lblClockValue, lblClockStatus, null, ACCENT_3));

        add(cards, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setBackground(BG_DARK);
        south.setBorder(new EmptyBorder(0, 16, 16, 16));

        log.setEditable(false);
        log.setBackground(CARD_BG);
        log.setForeground(TEXT_MAIN);
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        log.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane scroll = new JScrollPane(log);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(55, 59, 70)));
        scroll.setPreferredSize(new Dimension(100, 170));
        scroll.getViewport().setBackground(CARD_BG);

        JLabel logTitle = new JLabel("Jurnal evenimente");
        logTitle.setForeground(TEXT_DIM);
        logTitle.setFont(FONT_STATUS);
        logTitle.setBorder(new EmptyBorder(0, 2, 4, 0));

        JPanel logWrap = new JPanel(new BorderLayout());
        logWrap.setBackground(BG_DARK);
        logWrap.add(logTitle, BorderLayout.NORTH);
        logWrap.add(scroll, BorderLayout.CENTER);

        styleButton(btnStart, new Color(60, 150, 90));
        styleButton(btnStop, new Color(200, 70, 70));
        btnStop.setEnabled(false);

        btnStart.addActionListener(e -> {
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            startTimers();
        });
        btnStop.addActionListener(e -> stopAll());

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setBackground(BG_DARK);
        buttons.add(btnStart);
        buttons.add(btnStop);

        south.add(logWrap, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        setSize(900, 560);
        setLocationRelativeTo(null);
    }

    private JPanel buildCard(String title, String subtitle, JLabel valueLabel, JLabel statusLabel,
                             JProgressBar bar, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1, true),
                new EmptyBorder(16, 14, 16, 14)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(accent);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_STATUS);
        lblSub.setForeground(TEXT_DIM);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSub.setBorder(new EmptyBorder(2, 0, 12, 0));

        valueLabel.setFont(FONT_VALUE);
        valueLabel.setForeground(TEXT_MAIN);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel.setFont(FONT_STATUS);
        statusLabel.setForeground(accent);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        card.add(lblTitle);
        card.add(lblSub);
        card.add(valueLabel);

        if (bar != null) {
            bar.setAlignmentX(Component.CENTER_ALIGNMENT);
            bar.setMaximumSize(new Dimension(220, 10));
            bar.setForeground(accent);
            bar.setBackground(new Color(50, 54, 64));
            bar.setBorderPainted(false);
            bar.setBorder(new EmptyBorder(10, 0, 0, 0));
            card.add(bar);
        }

        card.add(statusLabel);
        return card;
    }

    private void styleButton(JButton b, Color bg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void write(String msg) {
        String stamp = TIME_FMT.format(new Date());
        System.out.println("[" + stamp + "] " + msg);
        SwingUtilities.invokeLater(() -> log.append("[" + stamp + "] " + msg + "\n"));
    }

    /* ================= NOTIFICARE DE SISTEM ================= */
    private void setupTray() {
        if (!SystemTray.isSupported()) {
            trayIcon = null;
            return;
        }
        try {
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.image.BufferedImage img =
                    new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(ACCENT_3);
            g.fillOval(1, 1, 14, 14);
            g.setColor(Color.WHITE);
            g.fillRect(7, 3, 2, 6);
            g.fillRect(7, 8, 5, 2);
            g.dispose();

            trayIcon = new TrayIcon(img, "Timer App");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (Exception ex) {
            trayIcon = null;
        }
    }

    /** Afișează o notificare de sistem; dacă tray-ul nu e disponibil, arată un dialog. */
    private void notifyClockFired() {
        String titlu = "Timer 3 declanșat";
        String mesaj = "Ora programată (" + TIME_FMT.format(oraTinta) + ") a sosit!";
        if (trayIcon != null) {
            trayIcon.displayMessage(titlu, mesaj, TrayIcon.MessageType.INFO);
        } else {
            JOptionPane.showMessageDialog(this, mesaj, titlu, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /* ================= TIMERELE ================= */
    private void startTimers() {
        timerInterval = new Timer();
        timerDelay = new Timer();
        timerClock = new Timer();
        uiTicker = new Timer();

        counter = 0;
        delayRemaining = 5;
        lblCounterValue.setText("0");
        lblCounterStatus.setText("Rulează...");
        barCounter.setValue(0);
        lblDelayValue.setText("5");
        lblDelayStatus.setText("Numărătoare inversă...");
        barDelay.setValue(0);
        lblClockStatus.setText("Se apropie...");

        write("Start timere");

        // 1. Reacționează cu o perioadă indicată (rată fixă, la fiecare 1 secundă)
        timerInterval.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                counter++;
                Toolkit.getDefaultToolkit().beep();
                SwingUtilities.invokeLater(() -> {
                    lblCounterValue.setText(String.valueOf(counter));
                    barCounter.setValue(counter % 100);
                });
                write("[Timer 1] Tick periodic nr. " + counter);
            }
        }, 0, 1000);

        // 2. Reacționează după un anumit interval de timp (o singură dată, 5 secunde)
        final int delayTotal = 5;
        barDelay.setMaximum(delayTotal);
        uiTicker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (delayRemaining <= 0) return;
                delayRemaining--;
                int elapsed = delayTotal - delayRemaining;
                SwingUtilities.invokeLater(() -> {
                    lblDelayValue.setText(String.valueOf(delayRemaining));
                    barDelay.setValue(elapsed);
                });
            }
        }, 1000, 1000);

        timerDelay.schedule(new TimerTask() {
            @Override
            public void run() {
                write("[Timer 2] Au trecut 5 secunde \u2014 opresc timerul periodic");
                SwingUtilities.invokeLater(() -> {
                    lblDelayValue.setText("0");
                    lblDelayStatus.setText("Executat!");
                    barDelay.setValue(delayTotal);
                });
                timerInterval.cancel();
            }
        }, delayTotal * 1000L);

        // 3. Reacționează la un anumit timp (oră exactă) + notificare de sistem
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10); // peste 10 secunde, pentru demonstrație
        // Pentru o oră fixă reală se folosește, de exemplu:
        // calendar.set(Calendar.HOUR_OF_DAY, 18);
        // calendar.set(Calendar.MINUTE, 30);
        // calendar.set(Calendar.SECOND, 0);
        oraTinta = calendar.getTime();
        lblClockValue.setText(TIME_FMT.format(oraTinta));

        timerClock.schedule(new TimerTask() {
            @Override
            public void run() {
                write("[Timer 3] Ora programată a sosit: " + TIME_FMT.format(oraTinta));
                SwingUtilities.invokeLater(() -> {
                    lblClockStatus.setText("Declanșat!");
                    notifyClockFired();
                });
            }
        }, oraTinta);

        write("Timer 3 programat pentru: " + TIME_FMT.format(oraTinta));
    }

    private void stopAll() {
        if (timerInterval != null) timerInterval.cancel();
        if (timerDelay != null) timerDelay.cancel();
        if (timerClock != null) timerClock.cancel();
        if (uiTicker != null) uiTicker.cancel();
        write("Toate timerele au fost oprite forțat (cancel()).");
        lblCounterStatus.setText("Oprit");
        lblDelayStatus.setText("Oprit");
        lblClockStatus.setText("Oprit");
        btnStop.setEnabled(false);
        btnStart.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TimerApp().setVisible(true));
    }
}