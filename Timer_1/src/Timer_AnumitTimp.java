import java.util.TimerTask;
import java.time.LocalTime;
import java.awt.*;
import java.util.Timer;
import javax.swing.*;

public class Timer_AnumitTimp extends TimerTask {

    int ora;
    int minut;
    Timer timer;

    public Timer_AnumitTimp(int ora, int minut) {
        this.ora = ora;
        this.minut = minut;
    }

    @Override
    public void run() {
        LocalTime acum = LocalTime.now();

        if (acum.getHour() == ora && acum.getMinute() == minut) {
            Toolkit.getDefaultToolkit().beep();

            JOptionPane.showMessageDialog(
                    null,
                    "Timpul a sosit!",
                    "Timer la un anumit timp",
                    JOptionPane.INFORMATION_MESSAGE
            );

            timer.cancel();
        }
    }

    public void start() {
        timer = new Timer();
        timer.scheduleAtFixedRate(this, 0, 1000);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
    }