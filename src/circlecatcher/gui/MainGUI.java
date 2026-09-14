package circlecatcher.gui;

import javax.swing.*;
import java.awt.*;

public class MainGUI {
    private JFrame frame;

    public MainGUI() {

        frame = new JFrame("Circle Catcher 3000");

        // create right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(Color.LIGHT_GRAY);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS)); // stacks vertically
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        // add rightPanel to the frame
        frame.getContentPane().add(rightPanel, BorderLayout.EAST);

        // create center panel
        JPanel centerPanel = new JPanel();
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

        SpinnerNumberModel model = new SpinnerNumberModel(1000, 1, 10000, 50);
        // 1000 ms default, 1 minimum, 10 seconds max, 50 ms spinning
        JSpinner periodSpinner = new JSpinner(model);
        periodSpinner.setFont(bigFont);
        periodPanel.add(periodSpinner);

        JButton startButton = new JButton("Start");
        startButton.setFont(bigFont);
        startPanel.add(startButton);

    }

    public void run() {
        // display it
        frame.pack();
        frame.setVisible(true);
    }
}
