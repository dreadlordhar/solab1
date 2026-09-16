import java.awt.FlowLayout;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class TimerInterval extends JPanel {
    private final JSpinner secunde = new JSpinner(new SpinnerNumberModel(5, 1, 3600, 1));
    private final JLabel stare = new JLabel("Oprit");
    private final JButton start = new JButton("Start");
    private final JButton stop = new JButton("Stop");
    private Timer timer;

    public TimerInterval() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 20));
        setBorder(BorderFactory.createTitledBorder("1. Actiune dupa un interval"));
        add(new JLabel("Intarziere (secunde):"));
        add(secunde);
        add(start);
        add(stop);
        add(stare);
        stop.setEnabled(false);
        start.addActionListener(e -> porneste());
        stop.addActionListener(e -> opreste());
    }

    private void porneste() {
        try {
            secunde.commitEdit();
        } catch (java.text.ParseException e) {
            stare.setText("Introdu un numar intre 1 si 3600.");
            return;
        }
        int durata = ((Number) secunde.getValue()).intValue();
        if (durata < 1 || durata > 3600) {
            stare.setText("Introdu un numar intre 1 si 3600.");
            return;
        }
        opreste();
        Timer pornit = new Timer("timer-interval", true);
        timer = pornit;
        start.setEnabled(false);
        secunde.setEnabled(false);
        stop.setEnabled(true);
        stare.setText("Astept " + durata + " secunde...");
        pornit.schedule(new TimerTask() {
            @Override
            public void run() {
                pornit.cancel();
                SwingUtilities.invokeLater(() -> {
                    // Ignoram rezultatul daca intre timp timerul a fost oprit.
                    if (timer != pornit) return;
                    opreste();
                    stare.setText("Executat dupa " + durata + " secunde.");
                });
            }
        }, durata * 1000L);
    }

    public void opreste() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        start.setEnabled(true);
        secunde.setEnabled(true);
        stop.setEnabled(false);
        stare.setText("Oprit");
    }
}
