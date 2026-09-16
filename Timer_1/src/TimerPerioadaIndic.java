import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class TimerPerioadaIndic {

    private final int VAL_MIN = 0;
    private final int VAL_MAX = 100;
    private final int PERIOADA = 100; // 10 pași pe secundă

    private JSlider slider;
    private long durataMilisecunde;
    private Timer timer;

    public TimerPerioadaIndic(JSlider slider, long durataMilisecunde) {
        this.slider = slider;
        this.durataMilisecunde = durataMilisecunde;
    }

    public void start() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int valoareCurenta = VAL_MIN;
            long timpStart = System.currentTimeMillis();

            @Override
            public void run() {
                valoareCurenta += 1;
                if (valoareCurenta > VAL_MAX) {
                    valoareCurenta = VAL_MIN; // resetarea la minim
                }

                int valFinal = valoareCurenta;
                SwingUtilities.invokeLater(() -> slider.setValue(valFinal));

                if (System.currentTimeMillis() - timpStart >= durataMilisecunde) {
                    timer.cancel();
                    System.out.println("Timer 2 s-a terminat!");
                }
            }
        }, 0, PERIOADA);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
}