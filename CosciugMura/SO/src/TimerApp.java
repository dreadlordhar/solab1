import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Lucrare de laborator 1 - Mecanism de planificare a activitatii proceselor
 * utilizand clasele Timer si TimerTask.
 *
 * Contine un singur timer, pornit manual printr-un buton "Start":
 *  - Timer cu rata fixa (scheduleAtFixedRate) - contor la fiecare 1 secunda
 */
public class TimerApp extends JFrame {

    // Paleta de culori
    private static final Color BG_DARK = new Color(24, 26, 32);
    private static final Color CARD_BG = new Color(34, 37, 46);
    private static final Color ACCENT_1 = new Color(88, 166, 255);   // albastru
    private static final Color TEXT_MAIN = new Color(230, 233, 240);
    private static final Color TEXT_DIM = new Color(150, 156, 168);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_STATUS = new Font("Segoe UI", Font.PLAIN, 12);
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss");

    private final JLabel lblCounterValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblCounterStatus = new JLabel("În așteptare...", SwingConstants.CENTER);
    private final JProgressBar barCounter = new JProgressBar(0, 100);

    private final JTextArea log = new JTextArea();
    private final JButton btnStart = new JButton("\u25B6  Start timer");
    private final JButton btnStop = new JButton("\u23F9  Oprește timerul");

    private Timer timerInterval;
    private int counter = 0;

    public TimerApp() {
        super("Planificarea proceselor \u2014 Timer & TimerTask");
        buildUI();
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

        JPanel cardWrap = new JPanel(new GridBagLayout());
        cardWrap.setBackground(BG_DARK);
        cardWrap.setBorder(new EmptyBorder(6, 16, 16, 16));
        JPanel card = buildCard("TIMER \u2014 Rată fixă", "Tick la fiecare 1 secundă",
                lblCounterValue, lblCounterStatus, barCounter, ACCENT_1);
        card.setPreferredSize(new Dimension(320, 220));
        cardWrap.add(card);
        add(cardWrap, BorderLayout.CENTER);

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
            startTimer();
        });
        btnStop.addActionListener(e -> stopTimer());

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setBackground(BG_DARK);
        buttons.add(btnStart);
        buttons.add(btnStop);

        south.add(logWrap, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        setSize(500, 520);
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

    /* ================= TIMERUL ================= */
    private void startTimer() {
        timerInterval = new Timer();
        counter = 0;
        lblCounterValue.setText("0");
        lblCounterStatus.setText("Rulează...");
        barCounter.setValue(0);

        write("Start timer (rată fixă, 1000 ms)");

        timerInterval.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                counter++;
                Toolkit.getDefaultToolkit().beep();
                SwingUtilities.invokeLater(() -> {
                    lblCounterValue.setText(String.valueOf(counter));
                    barCounter.setValue(counter % 100);
                });
                write("Tick periodic nr. " + counter);
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (timerInterval != null) timerInterval.cancel();
        write("Timerul a fost oprit forțat (cancel()).");
        lblCounterStatus.setText("Oprit");
        btnStop.setEnabled(false);
        btnStart.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TimerApp().setVisible(true));
    }
}