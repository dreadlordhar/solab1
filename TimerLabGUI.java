import java.util.Timer;
import java.util.Calendar;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;

public class TimerLabGUI extends JFrame {

    private JLabel labelInterval =
            new JLabel("Sesiune de studiu: se asteapta...");

    private JLabel labelMoment =
            new JLabel("Reminder: se asteapta...");

    private JLabel labelPeriodic =
            new JLabel("Timp de studiu: 0 secunde");

    private JButton butonStart = new JButton("Porneste");
    private JButton butonStop = new JButton("Opreste");

    private Timer timerInterval;
    private Timer timerMoment;
    private Timer timerPeriodic;

    public TimerLabGUI() {
        setTitle("Asistent pentru studiu");
        setLayout(new GridLayout(4, 1));

        add(labelInterval);
        add(labelMoment);
        add(labelPeriodic);

        JPanel panelButoane = new JPanel();

        panelButoane.add(butonStart);
        panelButoane.add(butonStop);

        add(panelButoane);

        butonStart.addActionListener(e -> pornesteTimere());
        butonStop.addActionListener(e -> opresteTimere());

        setSize(450, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void pornesteTimere() {

        opresteTimere();

        // 1. Timer dupa un interval de 5 secunde
        timerInterval = new Timer();

        timerInterval.schedule(
                new IntervalTask(labelInterval),
                5000
        );

        // 2. Timer la un moment fix
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 15);

        timerMoment = new Timer();

        timerMoment.schedule(
                new MomentTask(labelMoment),
                cal.getTime()
        );

        // 3. Timer periodic, la fiecare secunda
        timerPeriodic = new Timer();

        timerPeriodic.scheduleAtFixedRate(
                new PeriodicTask(labelPeriodic),
                0,
                1000
        );
    }

    private void opresteTimere() {

        if (timerInterval != null) {
            timerInterval.cancel();
        }

        if (timerMoment != null) {
            timerMoment.cancel();
        }

        if (timerPeriodic != null) {
            timerPeriodic.cancel();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimerLabGUI::new);
    }
}