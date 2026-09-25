package space;

import javax.swing.*;
import java.awt.*;

public class Interface extends JFrame {

    private JLabel fuelLabel;
    private JProgressBar fuelBar;
    private JLabel statusLabel;
    private JLabel eventLabel;

    private JButton startButton;
    private JButton stopButton;

    private FuelTimer fuelTimer;

    public Interface() {

        setTitle("Space Mission");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createInterface();
    }

    private void createInterface() {

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        );

        // Title
        JLabel titleLabel = new JLabel(
                "SPACE MISSION",
                SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(
                new BoxLayout(centerPanel, BoxLayout.Y_AXIS)
        );

        // Rocket
        JLabel rocketLabel = new JLabel(
                "🚀",
                SwingConstants.CENTER
        );
        rocketLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        rocketLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(rocketLabel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Fuel title
        JLabel fuelTitle = new JLabel("FUEL");
        fuelTitle.setFont(new Font("Arial", Font.BOLD, 16));
        fuelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(fuelTitle);

        // Fuel progress bar
        fuelBar = new JProgressBar(0, 100);
        fuelBar.setValue(100);
        fuelBar.setStringPainted(true);
        fuelBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        fuelBar.setMaximumSize(new Dimension(450, 30));

        centerPanel.add(fuelBar);

        // Fuel percentage
        fuelLabel = new JLabel("100%");
        fuelLabel.setFont(new Font("Arial", Font.BOLD, 18));
        fuelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(fuelLabel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Status
        statusLabel = new JLabel(
                "READY FOR LAUNCH",
                SwingConstants.CENTER
        );
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(statusLabel);
        centerPanel.add(Box.createVerticalStrut(15));

        // Space event
        eventLabel = new JLabel(
                "Waiting for space event...",
                SwingConstants.CENTER
        );
        eventLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(eventLabel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();

        startButton = new JButton("START MISSION");
        stopButton = new JButton("STOP");

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Button actions
        startButton.addActionListener(e -> startMission());

        stopButton.addActionListener(e -> stopMission());
    }

    private void startMission() {

        statusLabel.setText("MISSION STARTED");

        startButton.setEnabled(false);

        fuelTimer = new FuelTimer(

                // Actualizarea combustibilului
                fuel -> SwingUtilities.invokeLater(() -> {

                    fuelBar.setValue(fuel);
                    fuelLabel.setText(fuel + "%");

                }),

                // Evenimentul spațial
                () -> SwingUtilities.invokeLater(() -> {

                    eventLabel.setText(
                            "🛰 Satellite separation!"
                    );

                    statusLabel.setText(
                            "SATELLITE SEPARATED"
                    );

                })
        );

        fuelTimer.start();
    }

    private void stopMission() {

        if (fuelTimer != null) {
            fuelTimer.stop();
        }

        statusLabel.setText("MISSION STOPPED");
        startButton.setEnabled(true);
    }

}
