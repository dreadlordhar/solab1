import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

public class Interfata {

    static JLabel labelTimer1;
    static JLabel labelTimer2;
    static JSlider sliderTimer2;
    static JLabel labelTimer3;
    static JButton butonStart3, butonStop3;

    static Interval_timer timer1;
    static TimerPerioadaIndic timer2;
    static Timer_AnumitTimp timer3;

    // Boxe noi pentru countdown
    static JLabel countdownTimer1;
    static JLabel countdownTimer2;
    static JLabel countdownTimer3;

    // Variabile ajutătoare pentru calculul timpului rămas
    static long timer1StartMillis;
    static long timer2StartMillis;
    static long timer2DurataMillis;
    static LocalTime timer3Tinta;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Interfață - 3 Timere");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 380);
        frame.setLayout(new GridLayout(3, 1, 10, 10));

        // Interf Timer 1
        JPanel panouT1 = new JPanel(new BorderLayout());
        panouT1.setBorder(BorderFactory.createTitledBorder("Timer 1 - Interval_timer (15s)"));
        labelTimer1 = new JLabel("Pornit automat...", SwingConstants.CENTER);
        countdownTimer1 = new JLabel("Rămân: -- s", SwingConstants.CENTER);
        panouT1.add(labelTimer1, BorderLayout.CENTER);
        panouT1.add(countdownTimer1, BorderLayout.SOUTH);
        frame.add(panouT1);

        //  Interf Timer 2
        JPanel panouT2 = new JPanel(new BorderLayout());
        panouT2.setBorder(BorderFactory.createTitledBorder("Timer 2 - TimerPerioadaIndic (1 oră)"));
        sliderTimer2 = new JSlider(0, 100, 0);
        sliderTimer2.setPaintTicks(true);
        sliderTimer2.setMajorTickSpacing(10);
        sliderTimer2.setPaintLabels(true);
        labelTimer2 = new JLabel("Așteaptă jumătatea Timer 1...", SwingConstants.CENTER);
        countdownTimer2 = new JLabel("Rămân: -- ", SwingConstants.CENTER);
        panouT2.add(labelTimer2, BorderLayout.NORTH);
        panouT2.add(sliderTimer2, BorderLayout.CENTER);
        panouT2.add(countdownTimer2, BorderLayout.SOUTH);
        frame.add(panouT2);

        // Timer 3
        JPanel panouT3 = new JPanel(new BorderLayout());
        panouT3.setBorder(BorderFactory.createTitledBorder("Timer 3 - Timer_AnumitTimp (Start/Stop)"));
        labelTimer3 = new JLabel("Oprit", SwingConstants.CENTER);
        countdownTimer3 = new JLabel("Rămân: -- s", SwingConstants.CENTER);
        JPanel butoane3 = new JPanel();
        butonStart3 = new JButton("Start");
        butonStop3 = new JButton("Stop");
        butoane3.add(butonStart3);
        butoane3.add(butonStop3);
        panouT3.add(labelTimer3, BorderLayout.CENTER);
        panouT3.add(countdownTimer3, BorderLayout.NORTH);
        panouT3.add(butoane3, BorderLayout.SOUTH);
        frame.add(panouT3);

        // Logica

        // Timer 1
        timer1 = new Interval_timer(15);
        timer1.start();
        labelTimer1.setText("Timer 1 pornit (15s)...");
        timer1StartMillis = System.currentTimeMillis();

        // Countdown t1
        javax.swing.Timer countdown1 = new javax.swing.Timer(1000, null);
        countdown1.addActionListener(e -> {
            long trecut = (System.currentTimeMillis() - timer1StartMillis) / 1000;
            long ramase = 15 - trecut;
            if (ramase < 0) ramase = 0;
            countdownTimer1.setText("Rămân: " + ramase + " s");
            if (ramase == 0) ((javax.swing.Timer) e.getSource()).stop();
        });
        countdown1.start();

        // La jumate timp, pornim Timer 2
        javax.swing.Timer ceasIntermediar = new javax.swing.Timer(7500, e -> {
            labelTimer1.setText("Timer 1 rulează - Timer 2 a pornit!");
            labelTimer2.setText("Timer 2 pornit (1 oră)...");

            long oOra = 60L * 60L * 1000L; // 1 oră în milisecunde
            timer2 = new TimerPerioadaIndic(sliderTimer2, oOra);
            timer2.start();
            timer2StartMillis = System.currentTimeMillis();
            timer2DurataMillis = oOra;

            // Countdown t2
            javax.swing.Timer countdown2 = new javax.swing.Timer(1000, ev -> {
                long trecut = System.currentTimeMillis() - timer2StartMillis;
                long ramas = timer2DurataMillis - trecut;
                if (ramas < 0) ramas = 0;
                long ramasSecunde = ramas / 1000;
                long ore = ramasSecunde / 3600;
                long minute = (ramasSecunde % 3600) / 60;
                long secunde = ramasSecunde % 60;
                countdownTimer2.setText(String.format("Rămân: %02d:%02d:%02d", ore, minute, secunde));
                if (ramas == 0) ((javax.swing.Timer) ev.getSource()).stop();
            });
            countdown2.start();
        });
        ceasIntermediar.setRepeats(false);
        ceasIntermediar.start();

        // Timer 3
        butonStart3.addActionListener(e -> {
            LocalTime peste10secunde = LocalTime.now().plusSeconds(10);
            timer3 = new Timer_AnumitTimp(
                    peste10secunde.getHour(),
                    peste10secunde.getMinute(),
                    peste10secunde.getSecond()
            );
            timer3.start();
            labelTimer3.setText("Timer 3 pornit (țintă: " + peste10secunde + ")");
            timer3Tinta = peste10secunde;

            javax.swing.Timer countdown3 = new javax.swing.Timer(1000, ev -> {
                long ramase = LocalTime.now().until(timer3Tinta, java.time.temporal.ChronoUnit.SECONDS);
                if (ramase < 0) ramase = 0;
                countdownTimer3.setText("Rămân: " + ramase + " s");
                if (ramase == 0) ((javax.swing.Timer) ev.getSource()).stop();
            });
            countdown3.start();
        });

        butonStop3.addActionListener(e -> {
            if (timer3 != null) {
                timer3.stop();
                labelTimer3.setText("Timer 3 oprit manual");
                countdownTimer3.setText("Rămân: -- s");
            }
        });

        frame.setVisible(true);
    }
}