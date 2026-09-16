import java.awt.FlowLayout;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class TimerPeriodic extends JPanel {
    private final JSpinner perioada = new JSpinner(new SpinnerNumberModel(1000, 100, 60000, 100));
    private final JButton start = new JButton("Start");
    private final JButton stop = new JButton("Stop");
    private final JLabel stare = new JLabel("Oprit");
    private Timer timer;
    private int executii;
    private boolean indeplinit;

    public TimerPeriodic() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 20));
        setBorder(BorderFactory.createTitledBorder("3. Timer periodic"));
        add(new JLabel("Perioada (ms):"));
        add(perioada);
        add(start);
        add(stop);
        add(stare);
        stop.setEnabled(false);
        start.addActionListener(e -> porneste());
        stop.addActionListener(e -> opreste());
    }

    private void porneste() {
        try {
            perioada.commitEdit();
        } catch (java.text.ParseException e) {
            stare.setText("Introdu o perioada intre 100 si 60000 ms.");
            return;
        }
        int milisecunde = ((Number) perioada.getValue()).intValue();
        if (milisecunde < 100 || milisecunde > 60000) {
            stare.setText("Introdu o perioada intre 100 si 60000 ms.");
            return;
        }
        opreste();
        executii = 0;
        indeplinit = false;
        firePropertyChange("pornire", false, true);
        Timer pornit = new Timer("timer-periodic", true);
        timer = pornit;
        start.setEnabled(false);
        stop.setEnabled(true);
        perioada.setEnabled(false);
        stare.setText("Executii: 0 / 5");
        pornit.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (timer != pornit) return;
                    executii++;
                    indeplinit = executii >= 5;
                    stare.setText("Executii: " + executii
                        + (indeplinit ? " - conditie indeplinita" : " / 5"));
                });
            }
        }, milisecunde, milisecunde);
    }

    // Citita pe firul interfetei, la fel ca actualizarile contorului.
    public boolean isSarcinaIndeplinita() {
        return indeplinit;
    }

    public void opreste() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        start.setEnabled(true);
        stop.setEnabled(false);
        perioada.setEnabled(true);
        stare.setText("Oprit - executii: " + executii);
    }
}
