import java.util.Timer;
import java.util.TimerTask;

public class TimerPerioadaIndic {
    public static void main(String[] args) {
        // variabile
        final int VAL_MIN = 0;
        final int VAL_MAX = 100;
        final int VAL_INIT = 0;
        final int PERIOADA = 100; // 10 pași per secundă

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

                System.out.println("Valoare curentă: " + valoareCurenta);

                // Timer-ul se va opri dupa 13 secunde
                if (System.currentTimeMillis() - timpStart >= 13_000) {
                    timer.cancel();
                    System.out.println("Au trecut cele 13 secunde!");
                }
            }
        }, 0, PERIOADA);
    }
}