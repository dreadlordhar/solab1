package circlecatcher.gui;

import javax.swing.*;
import java.awt.*;

public class MainGUI {
    private JFrame frame;

    public MainGUI() {

        frame = new JFrame("Hello World Java Swing");

        // create right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(Color.LIGHT_GRAY);

        // add rightPanel to the frame
        frame.getContentPane().add(rightPanel, BorderLayout.EAST);

        // create center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.CYAN);

        // add centerPanel to the frame
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);

        // set frame site
        frame.setMinimumSize(new Dimension(1600, 900));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // center the JLabel
        JLabel lblText = new JLabel("Hello World!", SwingConstants.CENTER);

        // add JLabel to JFrame
        centerPanel.add(lblText);

    }

    public void run() {
        // display it
        frame.pack();
        frame.setVisible(true);
    }
}
