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

    public static void main(String[] args) {
        JFrame frame = new JFrame("La anumit timp");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 180);
        frame.setLayout(new GridLayout(4, 2, 5, 5));

        JLabel labelOra = new JLabel("Ora:");
        JTextField campOra = new JTextField();

        JLabel labelMinut = new JLabel("Minut:");
        JTextField campMinut = new JTextField();

        JLabel labelSecunda = new JLabel("Secunda:");
        JTextField campSecunda = new JTextField();

        JButton butonStart = new JButton("Start");

        frame.add(labelOra);
        frame.add(campOra);
        frame.add(labelMinut);
        frame.add(campMinut);
        frame.add(labelSecunda);
        frame.add(campSecunda);
        frame.add(butonStart);

        butonStart.addActionListener(e -> {
            try {
                int ora = Integer.parseInt(campOra.getText().trim());
                int minut = Integer.parseInt(campMinut.getText().trim());
                int secunda = Integer.parseInt(campSecunda.getText().trim());

                Timer_AnumitTimp timerAnumitTimp = new Timer_AnumitTimp(ora, minut, secunda);
                timerAnumitTimp.start();

                JOptionPane.showMessageDialog(frame, "Timer pornit pentru " + ora + ":" + minut + ":" + secunda);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Introdu valori numerice valide!", "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }
}