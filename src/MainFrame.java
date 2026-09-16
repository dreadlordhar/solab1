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
        JPanel continut = new JPanel(new GridLayout(2, 1, 10, 10));
        continut.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        continut.add(interval);
        continut.add(momentFix);
        setContentPane(continut);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                interval.opreste();
                momentFix.opreste();
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
