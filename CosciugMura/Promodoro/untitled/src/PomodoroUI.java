import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PomodoroUI extends JFrame {

    private JLabel labelTimer;
    private JLabel labelStatus;

    private JTextField fieldMinute;
    private JTextField fieldDelay;
    private JTextField fieldOra;

    private JButton buttonStart;
    private JButton buttonStop;

    private PomodoroService service;

    public PomodoroUI() {
        service = new PomodoroService();

        setTitle("Pomodoro Timer");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createUI();
    }

    private void createUI() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("POMODORO", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));

        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        labelTimer = new JLabel("25:00", SwingConstants.CENTER);
        labelTimer.setFont(new Font("Arial", Font.BOLD, 60));
        labelTimer.setAlignmentX(Component.CENTER_ALIGNMENT);

        labelStatus = new JLabel("Gata pentru o sesiune de concentrare");
        labelStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(labelTimer);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(labelStatus);
        centerPanel.add(Box.createVerticalStrut(25));

        JPanel pomodoroPanel = new JPanel(new FlowLayout());

        pomodoroPanel.setBorder(
                BorderFactory.createTitledBorder("Sesiune Pomodoro")
        );

        pomodoroPanel.add(new JLabel("Minute:"));

        fieldMinute = new JTextField("25", 5);
        pomodoroPanel.add(fieldMinute);

        buttonStart = new JButton("Start");
        buttonStop = new JButton("Stop");

        pomodoroPanel.add(buttonStart);
        pomodoroPanel.add(buttonStop);

        centerPanel.add(pomodoroPanel);

        JPanel delayPanel = new JPanel(new FlowLayout());

        delayPanel.setBorder(
                BorderFactory.createTitledBorder("Notificare după întârziere")
        );

        delayPanel.add(new JLabel("Secunde:"));

        fieldDelay = new JTextField("10", 5);

        JButton buttonDelay = new JButton("Programează");

        delayPanel.add(fieldDelay);
        delayPanel.add(buttonDelay);

        centerPanel.add(delayPanel);

        JPanel exactTimePanel = new JPanel(new FlowLayout());

        exactTimePanel.setBorder(
                BorderFactory.createTitledBorder("Alarmă la ora exactă")
        );

        exactTimePanel.add(new JLabel("Ora:"));

        fieldOra = new JTextField("18:00", 5);

        JButton buttonExactTime = new JButton("Programează");

        exactTimePanel.add(fieldOra);
        exactTimePanel.add(buttonExactTime);

        centerPanel.add(exactTimePanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        buttonStart.addActionListener(e -> startPomodoro());

        buttonStop.addActionListener(e -> stopTimers());

        buttonDelay.addActionListener(e -> scheduleDelay());

        buttonExactTime.addActionListener(e -> scheduleExactTime());
    }

    private void startPomodoro() {

        try {
            int minute = Integer.parseInt(fieldMinute.getText());

            if (minute <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Introduceți un număr pozitiv de minute."
                );
                return;
            }

            int seconds = minute * 60;

            service.startPomodoro(seconds, new PomodoroService.TimerCallback() {

                @Override
                public void onTick(int secondsLeft) {

                    SwingUtilities.invokeLater(() -> {

                        int minute = secondsLeft / 60;
                        int second = secondsLeft % 60;

                        labelTimer.setText(
                                String.format("%02d:%02d", minute, second)
                        );

                        labelStatus.setText("Sesiune de concentrare în desfășurare...");
                    });
                }

                @Override
                public void onDelayComplete(String msg) {
                }

                @Override
                public void onExactTimeReached(String msg) {
                }
            });

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Introduceți un număr valid."
            );
        }
    }

    private void scheduleDelay() {

        try {

            int seconds = Integer.parseInt(fieldDelay.getText());

            if (seconds <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Introduceți un număr pozitiv de secunde."
                );
                return;
            }

            service.scheduleNotificationDelay(
                    seconds,
                    new PomodoroService.TimerCallback() {

                        @Override
                        public void onTick(int secondsLeft) {
                        }

                        @Override
                        public void onDelayComplete(String msg) {

                            SwingUtilities.invokeLater(() -> {

                                labelStatus.setText(msg);

                                JOptionPane.showMessageDialog(
                                        PomodoroUI.this,
                                        msg,
                                        "Notificare",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            });
                        }

                        @Override
                        public void onExactTimeReached(String msg) {
                        }
                    }
            );

            labelStatus.setText(
                    "Notificarea a fost programată pentru " +
                    seconds + " secunde."
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Introduceți un număr valid."
            );
        }
    }

    private void scheduleExactTime() {

        try {

            String text = fieldOra.getText();

            SimpleDateFormat format =
                    new SimpleDateFormat("HH:mm");

            format.setLenient(false);

            Date inputDate = format.parse(text);

            Calendar calendar = Calendar.getInstance();

            Calendar target = Calendar.getInstance();

            target.set(
                    Calendar.HOUR_OF_DAY,
                    calendar.get(Calendar.HOUR_OF_DAY)
            );

            target.set(
                    Calendar.MINUTE,
                    calendar.get(Calendar.MINUTE)
            );

            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);

            Calendar entered = Calendar.getInstance();

            entered.setTime(inputDate);

            target.set(
                    Calendar.HOUR_OF_DAY,
                    entered.get(Calendar.HOUR_OF_DAY)
            );

            target.set(
                    Calendar.MINUTE,
                    entered.get(Calendar.MINUTE)
            );

            if (target.before(calendar)) {
                target.add(Calendar.DAY_OF_MONTH, 1);
            }

            Date targetTime = target.getTime();

            service.scheduleEndOfDayAlarm(
                    targetTime,
                    new PomodoroService.TimerCallback() {

                        @Override
                        public void onTick(int secondsLeft) {
                        }

                        @Override
                        public void onDelayComplete(String msg) {
                        }

                        @Override
                        public void onExactTimeReached(String msg) {

                            SwingUtilities.invokeLater(() -> {

                                labelStatus.setText(msg);

                                JOptionPane.showMessageDialog(
                                        PomodoroUI.this,
                                        msg,
                                        "Alarmă",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            });
                        }
                    }
            );

            labelStatus.setText(
                    "Alarma a fost programată pentru " + text
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Introduceți ora în formatul HH:mm\nExemplu: 18:30"
            );
        }
    }

    private void stopTimers() {

        service.stopAll();

        labelTimer.setText("00:00");
        labelStatus.setText("Toate timer-ele au fost oprite.");
    }
}
