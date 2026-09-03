package circlecatcher.gui;

import javax.swing.*;
import java.awt.*;

public class MainGUI {
    private JFrame frame;

    public MainGUI() {

        frame = new JFrame("Hello World Java Swing");

        // set frame site
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // center the JLabel
        JLabel lblText = new JLabel("Hello World!", SwingConstants.CENTER);

        // add JLabel to JFrame
        frame.getContentPane().add(lblText);

    }

    public void run() {
        // display it
        frame.pack();
        frame.setVisible(true);
    }
}
