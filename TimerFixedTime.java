import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

public class TimerFixedTime extends JPanel {
    private JLabel statusLabel;
    private JButton startButton;
    private Timer timer;

    public TimerFixedTime() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Timer la moment fix"));

        statusLabel = new JLabel("Apasa Start", SwingConstants.CENTER);
        startButton = new JButton("Start");

        add(statusLabel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startTimer());
    }

    private void startTimer() {
        startButton.setEnabled(false);
        timer = new Timer();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        statusLabel.setText("Programat pentru: " + calendar.getTime());

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Executat la: " + new java.util.Date());
                    startButton.setEnabled(true);
                });
                timer.cancel();
            }
        }, calendar.getTime());
    }
}