import javax.swing.*;
import java.awt.*;

public class TimerPanel extends JFrame {

    // Componentele interfeței
    private JLabel timerLabel;
    private JLabel statusLabel;

    private JSpinner minutesSpinner;
    private JSpinner secondsSpinner;

    private JProgressBar progressBar;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;

    public TimerPanel() {

        // Setările ferestrei
        setTitle("Space Ship Timer");
        setSize(550, 500);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Creăm interfața
        createInterface();
    }

    // Creăm toate componentele interfeței
    private void createInterface() {

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(10, 15, 40));
        mainPanel.setLayout(new BorderLayout(10, 10));

        // TITLU

        JLabel titleLabel = new JLabel(
                "SPACE SHIP TIMER",
                SwingConstants.CENTER
        );

        titleLabel.setForeground(new Color(100, 200, 255));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel subtitleLabel = new JLabel(
                "Decolarea navei spațiale • Time Management System",
                SwingConstants.CENTER
        );

        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel headerPanel = new JPanel(
                new GridLayout(2, 1)
        );

        headerPanel.setBackground(new Color(10, 15, 40));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        mainPanel.add(
                headerPanel,
                BorderLayout.NORTH
        );

        // CENTRUL FERESTREI

        JPanel centerPanel = new JPanel();

        centerPanel.setBackground(new Color(10, 15, 40));
        centerPanel.setLayout(
                new BoxLayout(centerPanel, BoxLayout.Y_AXIS)
        );

        // Afișăm timpul

        timerLabel = new JLabel(
                "00:00",
                SwingConstants.CENTER
        );

        timerLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        timerLabel.setForeground(Color.WHITE);

        timerLabel.setFont(
                new Font(
                        "Monospaced",
                        Font.BOLD,
                        70
                )
        );

        // Afișăm starea timerului

        statusLabel = new JLabel(
                "Ready for launch",
                SwingConstants.CENTER
        );

        statusLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        statusLabel.setForeground(
                new Color(150, 200, 255)
        );

        statusLabel.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );

        centerPanel.add(
                Box.createVerticalStrut(25)
        );

        centerPanel.add(timerLabel);

        centerPanel.add(
                Box.createVerticalStrut(10)
        );

        centerPanel.add(statusLabel);

        centerPanel.add(
                Box.createVerticalStrut(20)
        );

        // BARA DE PROGRES

        progressBar = new RoundedProgressBar(0, 100);

        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        progressBar.setPreferredSize(
                new Dimension(400, 25)
        );

        progressBar.setMaximumSize(
                new Dimension(400, 25)
        );

        progressBar.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        centerPanel.add(progressBar);

        centerPanel.add(
                Box.createVerticalStrut(25)
        );

        // SETĂRI TIMER

        JPanel settingsPanel = new JPanel(
                new FlowLayout()
        );

        settingsPanel.setBackground(
                new Color(10, 15, 40)
        );

        JLabel minutesLabel =
                new JLabel("Minutes:");

        minutesLabel.setForeground(Color.WHITE);

        JLabel secondsLabel =
                new JLabel("Seconds:");

        secondsLabel.setForeground(Color.WHITE);

        // Câmp pentru minute

        minutesSpinner = new JSpinner(
                new SpinnerNumberModel(
                        5,
                        0,
                        999,
                        1
                )
        );

        // Câmp pentru secunde

        secondsSpinner = new JSpinner(
                new SpinnerNumberModel(
                        0,
                        0,
                        59,
                        1
                )
        );

        minutesSpinner.setPreferredSize(
                new Dimension(60, 30)
        );

        secondsSpinner.setPreferredSize(
                new Dimension(60, 30)
        );

        minutesSpinner.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        secondsSpinner.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        settingsPanel.add(minutesLabel);
        settingsPanel.add(minutesSpinner);

        settingsPanel.add(secondsLabel);
        settingsPanel.add(secondsSpinner);

        centerPanel.add(settingsPanel);

        mainPanel.add(
                centerPanel,
                BorderLayout.CENTER
        );

        // BUTOANE

        JPanel buttonPanel = new JPanel(
                new FlowLayout()
        );

        buttonPanel.setBackground(
                new Color(10, 15, 40)
        );

        startButton = new JButton("START");
        pauseButton = new JButton("PAUSE");
        resetButton = new JButton("RESET");

        // Aplicăm stilul butoanelor
        styleButton(startButton);
        styleButton(pauseButton);
        styleButton(resetButton);

        // PAUSE este dezactivat inițial
        pauseButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resetButton);

        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        setContentPane(mainPanel);
    }

    // Stilizăm butoanele
    private void styleButton(JButton button) {

        button.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        button.setForeground(Color.WHITE);

        button.setBackground(
                new Color(30, 60, 100)
        );

        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setPreferredSize(
                new Dimension(120, 40)
        );
    }

    // GETTERS

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getPauseButton() {
        return pauseButton;
    }

    public JButton getResetButton() {
        return resetButton;
    }

    public JSpinner getMinutesSpinner() {
        return minutesSpinner;
    }

    public JSpinner getSecondsSpinner() {
        return secondsSpinner;
    }

    // ACTUALIZAREA INTERFEȚEI

    // Schimbăm timpul afișat
    public void setTimerText(String text) {

        timerLabel.setText(text);
    }

    // Schimbăm mesajul de stare
    public void setStatusText(String text) {

        statusLabel.setText(text);
    }

    // Schimbăm progresul
    public void setProgress(int value) {

        progressBar.setValue(value);
    }

    // Activăm sau dezactivăm câmpurile
    public void setSpinnersEnabled(boolean enabled) {

        minutesSpinner.setEnabled(enabled);
        secondsSpinner.setEnabled(enabled);
    }

    // Afișăm un mesaj
    public void showMessage(
            String message,
            String title
    ) {

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Afișăm un mesaj de avertizare
    public void showWarning(
            String message,
            String title
    ) {

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.WARNING_MESSAGE
        );
    }
}