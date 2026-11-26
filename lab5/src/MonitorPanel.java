
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.awt.image.BufferedImage;

public class MonitorPanel extends JPanel {

    private final JTextField folderField =
            new JTextField();

    private final JTextArea output =
            new JTextArea();

    private final JLabel status =
            new JLabel("Monitoring inactive");

    private final JLabel notification =
            new JLabel("No recent events");

    private final JButton startButton =
            new JButton("Start Monitoring");

    private final JButton stopButton =
            new JButton("Stop Monitoring");

    private final JButton selectButton =
            new JButton("Select Folder");

    private FolderMonitor monitor;
    private EventLogger logger;

    private TrayIcon trayIcon;
    private boolean trayUnavailable;

    private long session = 0;

    public MonitorPanel() {

        setLayout(new BorderLayout(10, 10));

        setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15));

        folderField.setEditable(false);

        JPanel folderPanel =
                new JPanel(new BorderLayout(10, 10));

        folderPanel.add(new JLabel("Folder:"),
                BorderLayout.WEST);

        folderPanel.add(folderField,
                BorderLayout.CENTER);

        folderPanel.add(selectButton,
                BorderLayout.EAST);

        add(folderPanel, BorderLayout.NORTH);

        JPanel buttons =
                new JPanel(new GridLayout(0, 2, 10, 10));

        JButton logButton =
                new JButton("Show Event Log");

        JButton clearButton =
                new JButton("Clear Output");

        buttons.add(startButton);
        buttons.add(stopButton);
        buttons.add(logButton);
        buttons.add(clearButton);

        stopButton.setEnabled(false);

        selectButton.addActionListener(e -> selectFolder());
        startButton.addActionListener(e -> startMonitoring());
        stopButton.addActionListener(e -> stopMonitoring());
        logButton.addActionListener(e -> showLogs());

        clearButton.addActionListener(e ->
                output.setText(""));

        output.setEditable(false);

        output.setFont(new Font(
                Font.MONOSPACED, Font.PLAIN, 13));

        output.setBackground(new Color(25, 30, 40));
        output.setForeground(new Color(150, 255, 170));

        JPanel center =
                new JPanel(new BorderLayout(10, 10));

        center.add(buttons, BorderLayout.NORTH);

        center.add(new JScrollPane(output),
                BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        JPanel information =
                new JPanel(new GridLayout(2, 1));

        information.add(status);
        information.add(notification);

        add(information, BorderLayout.SOUTH);

        try {

            logger = new EventLogger();

        } catch (IOException e) {

            output.append("Logger error: "
                    + e.getMessage() + "\n");

            startButton.setEnabled(false);
            logButton.setEnabled(false);
        }
    }

    private void selectFolder() {

        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(
                JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            folderField.setText(
                chooser.getSelectedFile().getAbsolutePath()
            );
        }
    }

    private void startMonitoring() {

        if (folderField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Please select a folder.");

            return;
        }

        try {

            Path folder = Paths.get(
                    folderField.getText()).toRealPath();

            if (folder.equals(
                    logger.getLogPath().getParent().toRealPath())) {

                throw new IOException(
                    "Cannot monitor the log directory.");
            }

            final long currentSession = ++session;

            monitor = new FolderMonitor(folder, event -> {

                String message;

                try {

                    message = logger.log(event);

                } catch (IOException e) {

                    message = event + " | Log error: "
                            + e.getMessage();
                }

                final String entry = message;

                SwingUtilities.invokeLater(() -> {

                    if (currentSession != session) {
                        return;
                    }

                    output.append(entry + "\n");

                    output.setCaretPosition(
                        output.getDocument().getLength());

                    notification.setText(
                        "Last event: " + event);

                    Toolkit.getDefaultToolkit().beep();
                    showWindowsNotification(event);
                    if (event.equals(
                            "Folder is no longer accessible.")) {
                        stopMonitoring();
                    }
                });
            });

            monitor.start();

            status.setText("Monitoring active");
            status.setForeground(new Color(0, 130, 0));

            startButton.setEnabled(false);
            selectButton.setEnabled(false);
            stopButton.setEnabled(true);

            output.append("Monitoring started: "
                    + folder + "\n");

        } catch (IOException | RuntimeException e) {

            output.append("Error: "
                    + e.getMessage() + "\n");
        }
    }

    public void stopMonitoring() {

        session++;

        try {

            if (monitor != null) {

                monitor.close();
                monitor = null;
            }

            output.append("Monitoring stopped.\n");

        } catch (IOException e) {

            output.append("Error: "
                    + e.getMessage() + "\n");

        } finally {

            status.setText("Monitoring inactive");
            status.setForeground(Color.RED);

            startButton.setEnabled(logger != null);
            selectButton.setEnabled(true);
            stopButton.setEnabled(false);

            if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
            }

trayUnavailable = false;
        }
    }

    private void showWindowsNotification(String event) {

        if (trayUnavailable || !SystemTray.isSupported()) {
            return;
        }

        try {

            if (trayIcon == null) {

                BufferedImage image = new BufferedImage(
                        32, 32, BufferedImage.TYPE_INT_ARGB);

                Graphics2D graphics = image.createGraphics();

                graphics.setColor(new Color(40, 110, 200));
                graphics.fillRoundRect(0, 0, 32, 32, 8, 8);

                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font(
                        "Arial", Font.BOLD, 20));

                graphics.drawString("F", 10, 24);
                graphics.dispose();

                trayIcon = new TrayIcon(
                        image, "System Security & Monitoring");

                trayIcon.setImageAutoSize(true);

                SystemTray.getSystemTray().add(trayIcon);
            }

            String title = "Folder Monitor";
            String message = event;

            if (event.startsWith("CREATED:")) {

                title = "File created";

            } else if (event.startsWith("MODIFIED:")) {

                title = "File modified";

            } else if (event.startsWith("DELETED:")) {

                title = "File deleted";
            }

            trayIcon.displayMessage(
                    title,
                    message,
                    TrayIcon.MessageType.INFO
            );

        } catch (AWTException | RuntimeException e) {

            trayIcon = null;
            trayUnavailable = true;

            output.append("Notification error: "
                    + e.getMessage() + "\n");
        }
    }
    
    private void showLogs() {

        try {

            output.setText(logger.readLogs());

            output.setCaretPosition(
                output.getDocument().getLength());

        } catch (IOException e) {

            output.append("Error reading log: "
                    + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame(
                    "Folder Monitor - Variant 20");

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE);

            frame.setSize(850, 600);
            frame.setLocationRelativeTo(null);

            frame.add(new MonitorPanel());

            frame.setVisible(true);
        });
    }
}
