import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimerPanel extends JFrame {

        // Componentele interfeței
        private JLabel timerLabel;
        private JLabel statusLabel;
        private JLabel currentTimeLabel;

        private JSpinner minutesSpinner;
        private JSpinner secondsSpinner;

        private JSpinner targetHourSpinner;
        private JSpinner targetMinuteSpinner;
        private JSpinner targetSecondSpinner;

        private JProgressBar progressBar;

        private JButton startButton;
        private JButton pauseButton;
        private JButton resetButton;
        private JButton switchTimerButton;

        private JPanel pagePanel;
        private JPanel countdownPage;
        private JPanel exactTimePage;
        private boolean exactTimeVisible;
        private final DefaultListModel<String> historyModel = new DefaultListModel<>();
        private final java.util.List<int[]> historyValues = new java.util.ArrayList<>();
        private JList<String> historyList;
        private JButton deleteHistoryButton;
        private final DateTimeFormatter clockFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

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
                                SwingConstants.CENTER);

                titleLabel.setForeground(new Color(100, 200, 255));
                titleLabel.setFont(new Font("Arial", Font.BOLD, 30));

                JLabel subtitleLabel = new JLabel(
                                "Decolarea navei spațiale • Time Management System",
                                SwingConstants.CENTER);

                subtitleLabel.setForeground(Color.LIGHT_GRAY);
                subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                JPanel headerPanel = new JPanel(
                                new GridLayout(2, 1));

                headerPanel.setBackground(new Color(10, 15, 40));

                headerPanel.add(titleLabel);
                headerPanel.add(subtitleLabel);

                mainPanel.add(
                                headerPanel,
                                BorderLayout.NORTH);

                // CENTRUL FERESTREI

                JPanel centerPanel = new JPanel();

                centerPanel.setBackground(new Color(10, 15, 40));
                centerPanel.setLayout(
                                new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

                // Afișăm timpul

                timerLabel = new JLabel(
                                "00:00",
                                SwingConstants.CENTER);

                timerLabel.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                timerLabel.setForeground(Color.WHITE);

                timerLabel.setFont(
                                new Font(
                                                "Monospaced",
                                                Font.BOLD,
                                                70));

                // Afișăm starea timerului

                statusLabel = new JLabel(
                                "Ready for launch",
                                SwingConstants.CENTER);

                statusLabel.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                statusLabel.setForeground(
                                new Color(150, 200, 255));

                statusLabel.setFont(
                                new Font(
                                                "Arial",
                                                Font.PLAIN,
                                                16));

                centerPanel.add(
                                Box.createVerticalStrut(25));

                centerPanel.add(timerLabel);

                centerPanel.add(
                                Box.createVerticalStrut(10));

                centerPanel.add(statusLabel);

                centerPanel.add(
                                Box.createVerticalStrut(20));

                // BARA DE PROGRES

                progressBar = new RoundedProgressBar(0, 100);

                progressBar.setValue(0);
                progressBar.setStringPainted(true);

                progressBar.setPreferredSize(
                                new Dimension(400, 25));

                progressBar.setMaximumSize(
                                new Dimension(400, 25));

                progressBar.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                centerPanel.add(progressBar);

                centerPanel.add(
                                Box.createVerticalStrut(25));

                // SETĂRI TIMER

                JPanel settingsPanel = new JPanel(
                                new FlowLayout());

                settingsPanel.setBackground(
                                new Color(10, 15, 40));

                JLabel minutesLabel = new JLabel("Minutes:");

                minutesLabel.setForeground(Color.WHITE);

                JLabel secondsLabel = new JLabel("Seconds:");

                secondsLabel.setForeground(Color.WHITE);

                // Câmp pentru minute

                minutesSpinner = new JSpinner(
                                new SpinnerNumberModel(
                                                5,
                                                0,
                                                999,
                                                1));

                // Câmp pentru secunde

                secondsSpinner = new JSpinner(
                                new SpinnerNumberModel(
                                                0,
                                                0,
                                                59,
                                                1));

                minutesSpinner.setPreferredSize(
                                new Dimension(60, 30));

                secondsSpinner.setPreferredSize(
                                new Dimension(60, 30));

                minutesSpinner.setFont(
                                new Font(
                                                "Arial",
                                                Font.BOLD,
                                                20));

                secondsSpinner.setFont(
                                new Font(
                                                "Arial",
                                                Font.BOLD,
                                                20));

                settingsPanel.add(minutesLabel);
                settingsPanel.add(minutesSpinner);

                settingsPanel.add(secondsLabel);
                settingsPanel.add(secondsSpinner);

                centerPanel.add(settingsPanel);

                countdownPage = centerPanel;

                exactTimePage = createExactTimePage();

                pagePanel = new JPanel(null);
                pagePanel.setBackground(new Color(10, 15, 40));
                pagePanel.add(countdownPage);
                pagePanel.add(exactTimePage);
                mainPanel.add(pagePanel, BorderLayout.CENTER);

                // BUTOANE

                JPanel buttonPanel = new JPanel(
                                new FlowLayout());

                buttonPanel.setBackground(
                                new Color(10, 15, 40));

                startButton = new JButton("START");
                pauseButton = new JButton("PAUSE");
                resetButton = new JButton("RESET");
                switchTimerButton = new JButton("TIMER 2  >>");

                // Aplicăm stilul butoanelor
                styleButton(startButton);
                styleButton(pauseButton);
                styleButton(resetButton);
                styleButton(switchTimerButton);
                switchTimerButton.setPreferredSize(new Dimension(145, 40));

                // PAUSE este dezactivat inițial
                pauseButton.setEnabled(false);

                buttonPanel.add(startButton);
                buttonPanel.add(pauseButton);
                buttonPanel.add(resetButton);
                buttonPanel.add(switchTimerButton);

                mainPanel.add(
                                buttonPanel,
                                BorderLayout.SOUTH);

                setContentPane(mainPanel);
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        @Override
                        public void componentResized(java.awt.event.ComponentEvent e) {
                                layoutPages();
                        }
                });
                layoutPages();
        }

        private JPanel createExactTimePage() {
                JPanel page = new JPanel();
                page.setBackground(new Color(10, 15, 40));
                page.setLayout(new BorderLayout(8, 8));

                JLabel title = new JLabel("EXACT LAUNCH TIME", SwingConstants.CENTER);
                title.setAlignmentX(Component.CENTER_ALIGNMENT);
                title.setForeground(Color.WHITE);
                title.setFont(new Font("Monospaced", Font.BOLD, 32));

                JLabel description = new JLabel("Timerul pornește la ora indicată", SwingConstants.CENTER);
                description.setAlignmentX(Component.CENTER_ALIGNMENT);
                description.setForeground(new Color(150, 200, 255));
                description.setFont(new Font("Arial", Font.PLAIN, 16));

                currentTimeLabel = new JLabel(LocalTime.now().format(clockFormat), SwingConstants.CENTER);
                currentTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                currentTimeLabel.setForeground(Color.WHITE);
                currentTimeLabel.setFont(new Font("Monospaced", Font.BOLD, 70));

                JPanel settings = new JPanel(new FlowLayout());
                settings.setBackground(new Color(10, 15, 40));
                Calendar now = Calendar.getInstance();
                targetHourSpinner = createSpinner(now.get(Calendar.HOUR_OF_DAY), 0, 23);
                targetMinuteSpinner = createSpinner(now.get(Calendar.MINUTE), 0, 59);
                targetSecondSpinner = createSpinner(now.get(Calendar.SECOND), 0, 59);
                addTimeField(settings, "Hour:", targetHourSpinner);
                addTimeField(settings, "Minute:", targetMinuteSpinner);
                addTimeField(settings, "Second:", targetSecondSpinner);

                JPanel top = new JPanel();
                top.setBackground(new Color(10, 15, 40));
                top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
                top.add(Box.createVerticalStrut(12));
                top.add(title);
                top.add(Box.createVerticalStrut(6));
                top.add(description);
                top.add(Box.createVerticalStrut(8));
                JPanel clockPanel = new JPanel(new BorderLayout());
                clockPanel.setBackground(new Color(10, 15, 40));
                clockPanel.add(currentTimeLabel, BorderLayout.CENTER);
                top.add(clockPanel);
                top.add(settings);
                page.add(top, BorderLayout.NORTH);

                historyList = new JList<>(historyModel);
                historyList.setVisibleRowCount(3);
                historyList.setFont(new Font("Monospaced", Font.PLAIN, 14));
                historyList.setBackground(new Color(25, 35, 65));
                historyList.setForeground(Color.WHITE);
                JScrollPane historyScroll = new JScrollPane(historyList);
                historyScroll.setBorder(BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(new Color(80, 160, 255)),
                                "Istoric programări"));
                page.add(historyScroll, BorderLayout.CENTER);

                JPanel historyActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                historyActions.setBackground(new Color(10, 15, 40));
                deleteHistoryButton = new JButton("ȘTERGE");
                styleButton(deleteHistoryButton);
                deleteHistoryButton.setPreferredSize(new Dimension(120, 32));
                historyActions.add(deleteHistoryButton);
                page.add(historyActions, BorderLayout.SOUTH);

                javax.swing.Timer clockTimer = new javax.swing.Timer(1000,
                                event -> currentTimeLabel.setText(LocalTime.now().format(clockFormat)));
                clockTimer.start();
                return page;
        }

        private JSpinner createSpinner(int value, int minimum, int maximum) {
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, minimum, maximum, 1));
                spinner.setPreferredSize(new Dimension(60, 30));
                spinner.setFont(new Font("Arial", Font.BOLD, 20));
                return spinner;
        }

        private void addTimeField(JPanel panel, String labelText, JSpinner spinner) {
                JLabel label = new JLabel(labelText);
                label.setForeground(Color.WHITE);
                panel.add(label);
                panel.add(spinner);
        }

        private void layoutPages() {
                if (pagePanel == null) {
                        return;
                }
                int width = pagePanel.getWidth();
                int height = pagePanel.getHeight();
                countdownPage.setBounds(exactTimeVisible ? -width : 0, 0, width, height);
                exactTimePage.setBounds(exactTimeVisible ? 0 : width, 0, width, height);
        }

        public void switchTimerPage(ActionListener actionListener) {
                switchTimerButton.addActionListener(actionListener);
        }

        public boolean isExactTimeVisible() {
                return exactTimeVisible;
        }

        public void animateToTimer(boolean showExactTime) {
                if (showExactTime == exactTimeVisible) {
                        return;
                }

                int width = pagePanel.getWidth();
                int height = pagePanel.getHeight();
                JPanel outgoing = exactTimeVisible ? exactTimePage : countdownPage;
                JPanel incoming = exactTimeVisible ? countdownPage : exactTimePage;
                int direction = showExactTime ? -1 : 1;
                incoming.setBounds(-direction * width, 0, width, height);

                final int[] offset = { 0 };
                javax.swing.Timer animation = new javax.swing.Timer(15, null);
                animation.addActionListener((ActionEvent event) -> {
                        offset[0] += Math.max(12, width / 18);
                        outgoing.setLocation(direction * offset[0], 0);
                        incoming.setLocation(-direction * width + direction * offset[0], 0);
                        pagePanel.repaint();
                        if (offset[0] >= width) {
                                animation.stop();
                                exactTimeVisible = showExactTime;
                                switchTimerButton.setText(showExactTime ? "<<  TIMER 1" : "TIMER 2  >>");
                                layoutPages();
                        }
                });
                animation.start();
        }

        // Stilizăm butoanele
        private void styleButton(JButton button) {

                button.setFont(
                                new Font(
                                                "Arial",
                                                Font.BOLD,
                                                14));

                button.setForeground(Color.WHITE);

                button.setBackground(
                                new Color(30, 60, 100));

                button.setFocusPainted(false);
                button.setBorderPainted(false);

                button.setPreferredSize(
                                new Dimension(120, 40));
        }

        // GETTERS

        public JButton getStartButton() {
                return startButton;
        }

        public JButton getPauseButton() {
                return pauseButton;
        }

        public void setPauseButtonVisible(boolean visible) {
                pauseButton.setVisible(visible);
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

        public int getTargetHour() {
                return (Integer) targetHourSpinner.getValue();
        }

        public int getTargetMinute() {
                return (Integer) targetMinuteSpinner.getValue();
        }

        public int getTargetSecond() {
                return (Integer) targetSecondSpinner.getValue();
        }

        public void setExactTimeSpinnersEnabled(boolean enabled) {
                targetHourSpinner.setEnabled(enabled);
                targetMinuteSpinner.setEnabled(enabled);
                targetSecondSpinner.setEnabled(enabled);
        }

        public void resetExactTimeToCurrent() {
                Calendar now = Calendar.getInstance();
                targetHourSpinner.setValue(now.get(Calendar.HOUR_OF_DAY));
                targetMinuteSpinner.setValue(now.get(Calendar.MINUTE));
                targetSecondSpinner.setValue(now.get(Calendar.SECOND));
        }

        public void addHistoryEntry(int hour, int minute, int second) {
                historyValues.add(new int[] { hour, minute, second });
                historyModel.addElement(formatHistoryTime(hour, minute, second));
        }

        public void deleteSelectedHistoryTime() {
                int index = historyList.getSelectedIndex();
                if (index >= 0) {
                        historyValues.remove(index);
                        historyModel.remove(index);
                }
        }

        private String formatHistoryTime(int hour, int minute, int second) {
                return String.format("Programare: %02d:%02d:%02d", hour, minute, second);
        }

        public void onDeleteHistory(ActionListener listener) {
                deleteHistoryButton.addActionListener(listener);
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
                        String title) {

                JOptionPane.showMessageDialog(
                                this,
                                message,
                                title,
                                JOptionPane.INFORMATION_MESSAGE);
        }

        // Afișăm un mesaj de avertizare
        public void showWarning(
                        String message,
                        String title) {

                JOptionPane.showMessageDialog(
                                this,
                                message,
                                title,
                                JOptionPane.WARNING_MESSAGE);
        }
}