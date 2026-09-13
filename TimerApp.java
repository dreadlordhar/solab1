import javax.swing.*;
import java.awt.*;

public class TimerApp {

    static IntervalTimer timer_interval;

    public static void main(String[] args) {

        JFrame fereastra = new JFrame();

        fereastra.setTitle("Timer pentru studiu");
        fereastra.setSize(500, 300);
        fereastra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fereastra.setLayout(new FlowLayout());

        JLabel titlu_interval = new JLabel("Timer cu interval");

        JLabel eticheta_interval_secunde = new JLabel("Interval:");

        JTextField camp_interval_secunde = new JTextField(5);

        JLabel eticheta_interval_secunde2 = new JLabel("secunde");

        JButton buton_interval_start = new JButton("START");

        buton_interval_start.addActionListener(e -> {

            int interval_secunde = Integer.parseInt(
                    camp_interval_secunde.getText()
            );

            timer_interval = new IntervalTimer(interval_secunde);

            timer_interval.start();
        });

        JButton buton_interval_stop = new JButton("STOP");

        buton_interval_stop.addActionListener(e -> {

            if (timer_interval != null) {
                timer_interval.stop();

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