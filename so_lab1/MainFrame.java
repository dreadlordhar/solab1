import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Aplicatie Timere");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 2, 10, 10));

        Task3Panel task3Panel = new Task3Panel();
        ConditionalTimerPanel conditionalPanel = new ConditionalTimerPanel(task3Panel);

        TimerInterval intervalPanel = new TimerInterval();
        TimerFixedTime fixedTimePanel = new TimerFixedTime();

        add(task3Panel);
        add(conditionalPanel);
        add(intervalPanel);
        add(fixedTimePanel);

        setSize(650, 500);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}