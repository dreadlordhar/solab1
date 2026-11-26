
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame(
                "System Security & Monitoring - Lab 5"
            );

            frame.setDefaultCloseOperation(
                JFrame.DO_NOTHING_ON_CLOSE
            );

            frame.setSize(1000, 700);
            frame.setMinimumSize(new Dimension(800, 500));
            frame.setLocationRelativeTo(null);

            NetworkPanel networkPanel =
                new NetworkPanel();

            MonitorPanel monitorPanel =
                new MonitorPanel();

            JTabbedPane tabs = new JTabbedPane();

            tabs.addTab(
                "Network & Firewall - Variant 8",
                networkPanel
            );

            tabs.addTab(
                "Folder Monitor - Variant 20",
                monitorPanel
            );

            JLabel title = new JLabel(
                "System Security & Monitoring",
                SwingConstants.CENTER
            );

            title.setFont(new Font(
                "Arial", Font.BOLD, 24
            ));

            title.setBorder(
                BorderFactory.createEmptyBorder(
                    15, 10, 10, 10
                )
            );

            JLabel authors = new JLabel(
                "Levinte Stanislav | Turcanu Damian | IA-242",
                SwingConstants.CENTER
            );

            authors.setBorder(
                BorderFactory.createEmptyBorder(
                    10, 10, 10, 10
                )
            );

            frame.setLayout(new BorderLayout());

            frame.add(title, BorderLayout.NORTH);
            frame.add(tabs, BorderLayout.CENTER);
            frame.add(authors, BorderLayout.SOUTH);

            frame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {

                    int answer =
                        JOptionPane.showConfirmDialog(
                            frame,
                            "Close the application?",
                            "Confirmation",
                            JOptionPane.YES_NO_OPTION
                        );

                    if (answer == JOptionPane.YES_OPTION) {

                        monitorPanel.stopMonitoring();

                        frame.dispose();

                        System.exit(0);
                    }
                }
            });

            frame.setVisible(true);
        });
    }
}
