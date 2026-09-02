import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        // Pornim interfața Swing
        SwingUtilities.invokeLater(() -> {

            // Creăm modelul, interfața și controllerul
            TimerModel model = new TimerModel();
            TimerPanel view = new TimerPanel();
            new TimerController(model, view);

            // Afișăm fereastra
            view.setVisible(true);
        });
    }
}