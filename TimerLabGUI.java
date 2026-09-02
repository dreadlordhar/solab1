import java.util.Timer;
import java.util.Calendar;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;

public class TimerLabGUI extends JFrame {
    private JLabel labelInterval = new JLabel("Interval: se asteapta...");
    private JLabel labelMoment   = new JLabel("Moment fix: se asteapta...");
    private JLabel labelPeriodic = new JLabel("Periodic: 0");

    public TimerLabGUI() {
        setTitle("Lab 1 - Timer si TimerTask");
        setLayout(new GridLayout(3, 1));
        add(labelInterval);
        add(labelMoment);
        add(labelPeriodic);
        setSize(320, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        pornesteTimere();
    }

    private void pornesteTimere() {
        // 1) dupa un interval fix, o singura data
        new Timer().schedule(new IntervalTask(labelInterval), 5000);

        // 2) la un moment exact (peste 15 secunde de acum)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 15);
        new Timer().schedule(new MomentTask(labelMoment), cal.getTime());

        // 3) cu perioada fixa, se repeta la fiecare secunda
        new Timer().scheduleAtFixedRate(new PeriodicTask(labelPeriodic), 0, 1000);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimerLabGUI::new);
    }
}