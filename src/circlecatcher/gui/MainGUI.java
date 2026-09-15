package circlecatcher.gui;

import javax.swing.*;

import circlecatcher.timers.Task;

import java.awt.*;
import java.util.Timer;

public class MainGUI {
    private JFrame frame;
    private final Timer mainTimer;
    private JPanel centerPanel;
    // added it here as a class variable so it's accessible by the circle spawner
    // game related variables for time and score tracking
    private boolean gameRunning;
    private int score;
    private long gameDuration;
    private long gameStartTime;


    public MainGUI() {
        mainTimer = new Timer();

        frame = new JFrame("Circle Catcher 3000");

        // create right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(Color.LIGHT_GRAY);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS)); // stacks vertically
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        // add rightPanel to the frame
        frame.getContentPane().add(rightPanel, BorderLayout.EAST);

        // create center panel. We pass null so there won't be any layout,
        // in order to draw dirrectly via coords of the CirclePanel.
        centerPanel = new JPanel(null);
        centerPanel.setBackground(Color.CYAN);


        // add centerPanel to the frame
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);

        // create three mini panels to the top of the right panel to host the buttons and the labels
        Dimension dimPanel = new Dimension(250, 100);
        // ... timerPanel
        JPanel timerPanel = new JPanel();
        timerPanel.setPreferredSize(dimPanel);
        timerPanel.setMaximumSize(dimPanel);
        timerPanel.setBackground(Color.YELLOW);


        // ... periodPanel
        JPanel periodPanel = new JPanel();
        periodPanel.setPreferredSize(dimPanel);
        periodPanel.setMaximumSize(dimPanel);
        periodPanel.setBackground(Color.YELLOW);


        // ... startPanel
        JPanel startPanel = new JPanel();
        startPanel.setPreferredSize(dimPanel);
        startPanel.setMaximumSize(dimPanel);
        startPanel.setBackground(Color.YELLOW);


        // add them to rightPanel
        rightPanel.add(timerPanel);
        rightPanel.add(Box.createVerticalStrut(25)); // 25px gap
        rightPanel.add(periodPanel);
        rightPanel.add(Box.createVerticalStrut(25));
        rightPanel.add(startPanel);


        // set frame site
        frame.setMinimumSize(new Dimension(1600, 900));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // FROM HERE WE ADD ONLY COMPONENTS TO EXISTENT PANELS
        // set font size
        Font bigFont = new Font("SansSerif", Font.PLAIN, 35);

        JLabel timerLabel = new JLabel("Time: 0s");
        timerLabel.setFont(bigFont);
        timerPanel.add(timerLabel);

        SpinnerNumberModel model = new SpinnerNumberModel(1000, 1, 10000, 1);
        JSpinner periodSpinner = new JSpinner(model);
        periodSpinner.setFont(bigFont);
        periodPanel.add(periodSpinner);

        JButton startButton = new JButton("Start");
        startButton.setFont(bigFont);

        // For colleagues. () -> {} defines an "lambda function", as in,
        // an anonymous functional interface that you pass as a callback for the button.
        // In other words, what you want that button to do in the first
        // place, without defining a new function/method/etc.

        startButton.addActionListener((e) -> {
            long startTime = System.currentTimeMillis();

            mainTimer.schedule( // TODO: REMOVE THIS ENTIRE LAMBDA TO SEPARATE CLASS
                Task.set(() -> {
                    long gameDuration = (Integer)periodSpinner.getValue() * 1000L;
                    long elapsed = System.currentTimeMillis() - startTime;
                    long seconds = elapsed / 1000;

                    SwingUtilities.invokeLater(() -> {
                        timerLabel.setText("Time: " + seconds + "s");
                    });

                    if (elapsed >= gameDuration) {
                        mainTimer.cancel();
                    }
                }),
                0,
                100
            );
        });
        startPanel.add(startButton);
    }

    public void run() {
        // display it
        frame.pack();
        frame.setVisible(true);
    }
}
