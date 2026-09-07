import java.util.*;
import java.awt.*;

class Atentie extends TimerTask {
    public void run() {
        Toolkit.getDefaultToolkit().beep();
        System.out.print(".");
    }
}

class Alarma extends TimerTask {
    public String mesaj;

    public Alarma(String mesaj) {
        this.mesaj = mesaj;
    }

    public void run() {
        System.out.println(mesaj);
    }
}

public class TestTimer {
    public static void main(String args[]) {
        // Fixed-rate repetitive action (beeps every second)
        final Timer t1 = new Timer();
        t1.scheduleAtFixedRate(
                new Atentie(), 0, 1*1000);

        // Anonymous class for another action (stops t1 after 10 seconds)
        Timer t2 = new Timer();
        t2.schedule(new TimerTask() {
            public void run() {
                System.out.println("Au trecut 10 secunde");
                t1.cancel();
            }
        }, 10*1000);

        // Schedule action for 13:15
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 0);
        Date ora = calendar.getTime();

        Timer t3 = new Timer();
        t3.schedule(new Alarma("Ora mesei!"), ora);
    }
}