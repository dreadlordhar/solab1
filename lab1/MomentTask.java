import java.util.TimerTask;
import javax.swing.JLabel;
import java.awt.Toolkit;

public class MomentTask extends TimerTask {
    private JLabel label;

    public MomentTask(JLabel label) {
        this.label = label;
    }

    @Override
    public void run() {
        label.setText("Reminder: verifica-ti sarcina!");
        Toolkit.getDefaultToolkit().beep();
    }
}