import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class Interval_timer extends TimerTask {

    private int secunde;
    private Timer timer;

    public Interval_timer(int secunde) {
        this.secunde = secunde;
    }

    @Override
    public void run() {
        Toolkit.getDefaultToolkit().beep();

        JOptionPane.showMessageDialog(
                null,
                "Timpul a expirat!",
                "Timer cu interval",
                JOptionPane.INFORMATION_MESSAGE
        );

        timer.cancel();
    }

    public void start() {
        timer = new Timer();
        timer.schedule(this, secunde * 1000L);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
}