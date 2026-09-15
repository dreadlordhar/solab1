import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

class Interval_timer extends TimerTask {

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
                "Timer",
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

    public static void main(String[] args) {

        JFrame fereastra = new JFrame("Timer pentru studiu");
        fereastra.setSize(500, 300);
        fereastra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fereastra.setLayout(new FlowLayout());

        JLabel titlu_interval = new JLabel("Timer cu interval");
        JLabel eticheta_interval_secunde = new JLabel("Interval:");
        JTextField camp_interval_secunde = new JTextField(5);
        JLabel eticheta_interval_secunde2 = new JLabel("secunde");

        JButton buton_interval_start = new JButton("START");
        JButton buton_interval_stop = new JButton("STOP");

        Interval_timer[] timer_interval = new Interval_timer[1];

        buton_interval_start.addActionListener(e -> {
            int interval_secunde =
                    Integer.parseInt(camp_interval_secunde.getText());

            timer_interval[0] =
                    new Interval_timer(interval_secunde);

            timer_interval[0].start();
        });

        buton_interval_stop.addActionListener(e -> {
            if (timer_interval[0] != null) {
                timer_interval[0].stop();

                JOptionPane.showMessageDialog(
                        fereastra,
                        "Timerul a fost oprit!",
                        "Timer cu interval",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        fereastra.add(titlu_interval);
        fereastra.add(eticheta_interval_secunde);
        fereastra.add(camp_interval_secunde);
        fereastra.add(eticheta_interval_secunde2);
        fereastra.add(buton_interval_start);
        fereastra.add(buton_interval_stop);

        fereastra.setVisible(true);
    }
}