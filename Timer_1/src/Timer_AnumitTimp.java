import java.awt.*;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class TimereCombinate {

    // ===== Timer 1: avanseaza un slider periodic =====
    static class TimerPerioadaIndic {
        final int VAL_MIN = 0;
        final int VAL_MAX = 100;
        final int VAL_MIJLOC = (VAL_MIN + VAL_MAX) / 2; // 50 - "mijlocul"
        final int PERIOADA = 100; // ms intre pasi

        JSlider sliderNou;
        Timer timer;
        boolean timerAlDoileaPornit = false;

        Runnable laMijloc; // ce se intampla cand slider-ul ajunge la mijloc

        TimerPerioadaIndic(JSlider slider, Runnable laMijloc) {
            this.sliderNou = slider;
            this.laMijloc = laMijloc;
        }

        void start() {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int valoareCurenta = VAL_MIN;
                long timpStart = System.currentTimeMillis();

                @Override
                public void run() {
                    valoareCurenta += 1;
                    if (valoareCurenta > VAL_MAX) {
                        valoareCurenta = VAL_MIN;
                    }

                    SwingUtilities.invokeLater(() -> sliderNou.setValue(valoareCurenta));

                    // cand ajungem la mijloc, pornim al doilea timer (o singura data)
                    if (valoareCurenta == VAL_MIJLOC && !timerAlDoileaPornit) {
                        timerAlDoileaPornit = true;
                        SwingUtilities.invokeLater(laMijloc);
                    }

                    if (System.currentTimeMillis() - timpStart >= 13_000) {
                        timer.cancel();
                        System.out.println("Au trecut cele 13 secunde!");
                    }
                }
            }, 0, PERIOADA);
        }
    }

    // ===== Timer 2: verifica in fiecare secunda daca s-a ajuns la ora exacta =====
    static class TimerAnumitTimp extends TimerTask {
        int ora, minut, secunda;
        Timer timer;

        TimerAnumitTimp(int ora, int minut, int secunda) {
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
                    JOptionPane.showMessageDialog(null, "Timpul a sosit!",
                            "Timer la un anumit timp", JOptionPane.INFORMATION_MESSAGE);
                });
            }
        }

        void start() {
            timer = new Timer();
            timer.scheduleAtFixedRate(this, 0, 1000);
        }
    }

    public static void main(String[] args) {
        JFrame fereastra = new JFrame("Timer periodic + Timer la ora exacta");
        fereastra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fereastra.setSize(400, 220);
        fereastra.setLayout(new BorderLayout());

        JSlider sliderNou = new JSlider(0, 100, 0);
        sliderNou.setPaintTicks(true);
        sliderNou.setMajorTickSpacing(10);
        sliderNou.setPaintLabels(true);
        fereastra.add(sliderNou, BorderLayout.CENTER);

        // Panou pentru introducerea orei-tinta a celui de-al doilea timer
        JPanel panouOra = new JPanel(new GridLayout(1, 6, 5, 5));
        JTextField campOra = new JTextField();
        JTextField campMinut = new JTextField();
        JTextField campSecunda = new JTextField();
        panouOra.add(new JLabel("Ora:"));
        panouOra.add(campOra);
        panouOra.add(new JLabel("Min:"));
        panouOra.add(campMinut);
        panouOra.add(new JLabel("Sec:"));
        panouOra.add(campSecunda);
        fereastra.add(panouOra, BorderLayout.NORTH);

        JLabel status = new JLabel("Timer.", SwingConstants.CENTER);
        fereastra.add(status, BorderLayout.SOUTH);

        fereastra.setVisible(true);

        // Ce se intampla cand primul timer ajunge la mijloc:
        Runnable laMijloc = () -> {
            try {
                int ora = Integer.parseInt(campOra.getText().trim());
                int minut = Integer.parseInt(campMinut.getText().trim());
                int secunda = Integer.parseInt(campSecunda.getText().trim());

                TimerAnumitTimp timerAnumitTimp = new TimerAnumitTimp(ora, minut, secunda);
                timerAnumitTimp.start();

                status.setText("Mijloc atins -> Timer la ora exacta pornit (" + ora + ":" + minut + ":" + secunda + ")");
            } catch (NumberFormatException ex) {
                status.setText("Mijloc atins, dar ora introdusa e invalida!");
            }
        };

        TimerPerioadaIndic timerPerioada = new TimerPerioadaIndic(sliderNou, laMijloc);
        timerPerioada.start();
    }
}