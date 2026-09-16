import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

public class TimerFixedTime extends JPanel {
    private final JSpinner moment = new JSpinner(new SpinnerDateModel(
        new Date(System.currentTimeMillis() + 60000), null, null, Calendar.SECOND));
    private final JLabel stare = new JLabel("Oprit");
    private final JButton start = new JButton("Start");
    private final JButton stop = new JButton("Stop");
    private Timer timer;

    public TimerFixedTime() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 20));
        setBorder(BorderFactory.createTitledBorder("2. Actiune la un moment fix"));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(moment, "dd.MM.yyyy HH:mm:ss");
        editor.getFormat().setLenient(false);
        moment.setEditor(editor);
        add(new JLabel("Data si ora:"));
        add(moment);
        add(start);
        add(stop);
        add(stare);
        stop.setEnabled(false);
        start.addActionListener(e -> porneste());
        stop.addActionListener(e -> opreste());
    }

    private void porneste() {
        try {
            moment.commitEdit();
        } catch (java.text.ParseException e) {
            stare.setText("Data sau ora nu este valida.");
            return;
        }
        Date ales = (Date) moment.getValue();
        if (!ales.after(new Date())) {
            stare.setText("Alege un moment din viitor.");
            return;
        }
        opreste();
        Timer pornit = new Timer("timer-moment-fix", true);
        timer = pornit;
        start.setEnabled(false);
        moment.setEnabled(false);
        stop.setEnabled(true);
        stare.setText("Programat: " + ora(ales));
        pornit.schedule(new TimerTask() {
            @Override
            public void run() {
                Date executat = new Date();
                pornit.cancel();
                SwingUtilities.invokeLater(() -> {
                    if (timer != pornit) return;
                    opreste();
                    stare.setText("Executat la " + ora(executat));
                });
            }
        }, ales);
    }

    private String ora(Date data) {
        return new SimpleDateFormat("HH:mm:ss").format(data);
    }

    public void opreste() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        start.setEnabled(true);
        moment.setEnabled(true);
        stop.setEnabled(false);
        stare.setText("Oprit");
    }
}
