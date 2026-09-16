import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("SO Lab 1 - IA-242_9");
        TimerInterval interval = new TimerInterval();
        TimerFixedTime momentFix = new TimerFixedTime();
        TimerPeriodic periodic = new TimerPeriodic();
        TimerConditionat conditionat = new TimerConditionat(periodic);
        JPanel continut = new JPanel(new GridLayout(4, 1, 10, 10));
        continut.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        continut.add(interval);
        continut.add(momentFix);
        continut.add(periodic);
        continut.add(conditionat);
        setContentPane(continut);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                interval.opreste();
                momentFix.opreste();
                periodic.opreste();
                conditionat.opreste();
            }
        });
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
