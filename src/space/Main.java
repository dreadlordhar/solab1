package space;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Interface window = new Interface();

            window.setVisible(true);
        });
    }
}