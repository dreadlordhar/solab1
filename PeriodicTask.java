import java.util.TimerTask;
import javax.swing.JLabel;

public class PeriodicTask extends TimerTask {
    private JLabel label;
    private int contor = 0;

    public PeriodicTask(JLabel label) {
        this.label = label;
    }

    @Override
    public void run() {
        contor++;
        label.setText("Periodic: " + contor);
    }
}