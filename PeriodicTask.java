import java.util.TimerTask;
import javax.swing.JLabel;
import java.awt.Toolkit;

public class PeriodicTask extends TimerTask {
    private JLabel label;
    private int contor = 0;

    public PeriodicTask(JLabel label) {
        this.label = label;
    }

    @Override
    public void run() {
        contor++;

        if (contor % 10 == 0) {
            label.setText("Pauza! Relaxeaza-te putin.");
            Toolkit.getDefaultToolkit().beep();
        } else {
            label.setText("Timp de studiu: " + contor + " secunde");
        }
    }
}