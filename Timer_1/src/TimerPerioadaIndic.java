import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JSlider;

public class TimerPerioadaIndic {
    public static void main(String[] args) {
        // variabile
        final int VAL_MIN = 0;
        final int VAL_MAX = 100;
        final int VAL_INIT = 0;
        final int PERIOADA = 100; // 10 pași per secundă)

        // Fereastra
        JFrame fereastra = new JFrame("Exemplu Timer + JSlider");
        fereastra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fereastra.setSize(400, 150);
        fereastra.setLayout(new BorderLayout());

        // Slider
        final JSlider sliderNou = new JSlider(VAL_MIN, VAL_MAX, VAL_INIT);
        sliderNou.setPaintTicks(true);      // afișăm liniile riglei
        sliderNou.setMajorTickSpacing(10);  // 10 diviziuni mari
        sliderNou.setPaintLabels(true);     // afișăm valorile

        fereastra.add(sliderNou, BorderLayout.CENTER);
        fereastra.setVisible(true);

        // Construirea timer-ului
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            int valoareCurenta = VAL_INIT;
            long timpStart = System.currentTimeMillis();

            public void run() {
                valoareCurenta += 1;
                if (valoareCurenta > VAL_MAX) {
                    valoareCurenta = VAL_MIN; // resetarea la minim
                }

                sliderNou.setValue(valoareCurenta);

                // Timer-ul se va opri dupa 13 secunde
                if (System.currentTimeMillis() - timpStart >= 13_000) {
                    timer.cancel();
                    System.out.println("Au trecut cele 13 secunde!");
                }
            }
        }, 0, PERIOADA);
    }
}