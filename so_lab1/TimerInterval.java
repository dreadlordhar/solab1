import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class TimerInterval extends JPanel {
    private JLabel statusLabel;
    private JButton startButton;
    private Timer timer;
    private int counter;

    public TimerInterval() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Timer la interval fix"));

        statusLabel = new JLabel("Apasa Start", SwingConstants.CENTER);
        startButton = new JButton("Start");

        add(statusLabel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startTimer());
    }

    private void startTimer() {
        startButton.setEnabled(false);
        counter = 0;
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                counter++;
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("Task rulat de " + counter + " ori"));

                if (counter >= 5) {
                    timer.cancel();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Timer oprit dupa 5 executii");
                        startButton.setEnabled(true);
                    });
                }
            }
        }, 1000, 2000);
    }
}