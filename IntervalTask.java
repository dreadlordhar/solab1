import java.util.TimerTask;
import javax.swing.JLabel;
import java.awt.Toolkit;

public class IntervalTask extends TimerTask {
    private JLabel label;

    public IntervalTask(JLabel label) {
        this.label = label;
    }

    @Override
    public void run() {
        label.setText("Sesiunea de studiu s-a terminat!");
        Toolkit.getDefaultToolkit().beep();
    }
}