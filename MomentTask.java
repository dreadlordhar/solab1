import java.util.TimerTask;
import javax.swing.JLabel;

public class MomentTask extends TimerTask {
    private JLabel label;

    public MomentTask(JLabel label) {
        this.label = label;
    }

    @Override
    public void run() {
        label.setText("Moment fix: am ajuns la ora tinta!");
    }
}