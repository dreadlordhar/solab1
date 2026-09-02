import java.util.TimerTask;
import javax.swing.JLabel;

public class IntervalTask extends TimerTask {
    private JLabel label;

    public IntervalTask(JLabel label) {
        this.label = label;
    }

    @Override
    public void run() {
        label.setText("Interval: au trecut 5 secunde!");
    }
}