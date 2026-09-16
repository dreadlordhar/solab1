import java.util.TimerTask;
import java.time.LocalTime;
import java.awt.*;
import java.util.Timer;
import javax.swing.*;

public class Timer_AnumitTimp extends TimerTask {

    int ora;
    int minut;
    int secunda;
    Timer timer;

    public Timer_AnumitTimp(int ora, int minut, int secunda) {
        this.ora = ora;
        this.minut = minut;
        this.secunda = secunda;
    }

    @Override
    public void run() {
        LocalTime acum = LocalTime.now();

        if (acum.getHour() == ora && acum.getMinute() == minut && acum.getSecond() == secunda) {
            timer.cancel();

            SwingUtilities.invokeLater(() -> {
                Toolkit.getDefaultToolkit().beep();

                JOptionPane.showMessageDialog(
                        null,
                        "Timpul a sosit!",
                        "Timer la un anumit timp",
                        JOptionPane.INFORMATION_MESSAGE
                );
            });
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