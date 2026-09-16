package circlecatcher.gui;

import javax.swing.*;

import circlecatcher.timers.Task;
import circlecatcher.Score;
import circlecatcher.distraction.DistractionManager;

import java.awt.*;
import java.util.Timer;

public class MainGUI {
    private JFrame frame;
    private final Timer mainTimer;
    private Timer spawnTimer;
    private JPanel centerPanel;
    // added it here as a class variable so it's accessible by the circle spawner
    // game related variables for time and score tracking
    private boolean gameRunning;
    private Score score;
    private long gameDuration;
    private long gameStartTime;
    private DistractionManager distractionManager;


    public MainGUI() {
        mainTimer = new Timer();
        spawnTimer = new Timer();
        score = new Score(0);
        distractionManager = new DistractionManager();

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

        JPanel scorePanel = new JPanel();
        scorePanel.setPreferredSize(dimPanel);
        scorePanel.setMaximumSize(dimPanel);
        scorePanel.setBackground(Color.YELLOW);


        // add them to rightPanel
        rightPanel.add(scorePanel);
        rightPanel.add(Box.createVerticalStrut(25));
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

        JLabel scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(bigFont);
        scorePanel.add(scoreLabel);

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
            if (!gameRunning) {
                // --- START the game ---
                gameRunning = true;
                gameStartTime = System.currentTimeMillis();
                gameDuration = 20000; // 20 seconds for testing
                startButton.setText("Stop");

                // schedule countdown timer (ticks every 100ms)
                mainTimer.schedule(
                    Task.set(() -> {
                        long elapsed = System.currentTimeMillis() - gameStartTime;
                        long remaining = (gameDuration - elapsed) / 1000;

                        SwingUtilities.invokeLater(() -> {
                            timerLabel.setText("Time: " + remaining + "s");
                            scoreLabel.setText("Score: " + score.getValue());
                        });

                        if (elapsed >= gameDuration) {
                            stopGame(startButton, timerLabel);
                        }
                    }),
                    0,
                    100
                );

                int spawnInterval = (Integer) periodSpinner.getValue();

                // here we spwan the circles with the periodic timer
                spawnTimer.schedule(
                    Task.set(() -> {
                        if (gameRunning) {
                            spawnCircle();
                        }
                    }),
                    0,
                    spawnInterval
                );

                // start distractions
                distractionManager.start(gameDuration);


            } else {
                // --- STOP the game (manual quit) ---
                stopGame(startButton, timerLabel);
            }
        });
        startPanel.add(startButton);
    }

    public void run() {
        // display it
        frame.pack();
        frame.setVisible(true);
    }

    private void stopGame(JButton startButton, JLabel timerLabel) {
        gameRunning = false;
        mainTimer.cancel();
        spawnTimer.cancel();
        distractionManager.stop();

        // remove all circles from the game area
        centerPanel.removeAll();
        centerPanel.repaint();

        // reset UI
        startButton.setText("Start");
        timerLabel.setText("Time: 0s");

        // teacher requirement: app closes when game ends
        frame.dispose();
        System.exit(0);
    }

    private void spawnCircle() {
        int diameter = 60;
        int padding = 5;

        int maxX = centerPanel.getWidth() - diameter;
        int maxY = centerPanel.getHeight() - diameter;

        if (maxX <= 0 || maxY <= 0) return;

        int x = (int) (Math.random() * maxX);
        int y = (int) (Math.random() * maxY);

        SwingUtilities.invokeLater(() -> {
            centerPanel.add(new CirclePanel(x, y, diameter, 0xFFFFFF, score));
            centerPanel.revalidate();
            centerPanel.repaint();
        });
    }
}


