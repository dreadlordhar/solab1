
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;

public class NetworkPanel extends JPanel {

    private final FirewallManager firewall =
            new FirewallManager();

    private final NetworkDiagnostics network =
            new NetworkDiagnostics();

    private final JTextField hostField =
            new JTextField("127.0.0.1", 15);

    private final JTextArea output =
            new JTextArea();

    private final JPanel buttons =
            new JPanel(new GridLayout(0, 2, 10, 10));

    public NetworkPanel() {

        setLayout(new BorderLayout(10, 10));

        setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15));

        JPanel settings = new JPanel(new GridLayout(
                2, 2, 10, 10));

        settings.add(new JLabel("IP / Host:"));
        settings.add(hostField);

        settings.add(new JLabel("TCP Port:"));
        settings.add(new JLabel("8080"));

        add(settings, BorderLayout.NORTH);

        addButton("Add Firewall Rule", () -> {

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Allow incoming TCP connections on port 8080?",
                    "Firewall confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {
                executeAsync(() -> firewall.addRule());
            }
        });

        addButton("Remove Firewall Rule", () -> {

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Remove the laboratory firewall rule?",
                    "Firewall confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {
                executeAsync(() -> firewall.removeRule());
            }
        });

        addButton("Ping", () -> {
            String host = hostField.getText().trim();
            executeAsync(() -> network.ping(host));
        });

        addButton("Tracert", () -> {
            String host = hostField.getText().trim();
            executeAsync(() -> network.traceroute(host));
        });

        addButton("IP Config", () ->
            executeAsync(() -> network.ipConfig())
        );

        addButton("Check Port 8080", () -> {
            String host = hostField.getText().trim();

            executeAsync(() ->
                network.checkPort(host, 8080));
        });

        addButton("Clear Output", () ->
            output.setText("")
        );

        output.setEditable(false);

        output.setFont(new Font(
                Font.MONOSPACED, Font.PLAIN, 13));

        output.setBackground(new Color(25, 30, 40));
        output.setForeground(new Color(150, 255, 170));

        JScrollPane scroll = new JScrollPane(output);

        JPanel center = new JPanel(
                new BorderLayout(10, 10));

        center.add(buttons, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private void addButton(String title, Runnable action) {

        JButton button = new JButton(title);

        button.addActionListener(e -> action.run());

        buttons.add(button);
    }

    private void setButtonsEnabled(boolean enabled) {

        for (Component component : buttons.getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void executeAsync(Callable<String> task) {

        output.append("\nExecuting command...\n");

        setButtonsEnabled(false);

        SwingWorker<String, Void> worker =
                new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground()
                    throws Exception {

                return task.call();
            }

            @Override
            protected void done() {

                try {
                    output.append(get() + "\n");

                } catch (Exception e) {

                    Throwable cause = e.getCause();

                    output.append("Error: " +
                            (cause != null
                            ? cause.getMessage()
                            : e.getMessage()) + "\n");

                } finally {

                    setButtonsEnabled(true);

                    output.setCaretPosition(
                        output.getDocument().getLength()
                    );
                }
            }
        };

        worker.execute();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame(
                    "Network & Firewall - Variant 8");

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE);

            frame.setSize(850, 600);
            frame.setLocationRelativeTo(null);

            frame.add(new NetworkPanel());

            frame.setVisible(true);
        });
    }
}
