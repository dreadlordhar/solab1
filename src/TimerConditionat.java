import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class TimerConditionat extends JPanel {
    private final TimerPeriodic periodic;
    private final JLabel stare = new JLabel();
    private final Indicator indicator = new Indicator();
    private Timer timer;

    public TimerConditionat(TimerPeriodic periodic) {
        this.periodic = periodic;
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 20));
        setBorder(BorderFactory.createTitledBorder("4. Timer conditionat"));
        add(indicator);
        add(stare);
        periodic.addPropertyChangeListener("pornire", e -> asteapta());
        asteapta();
    }

    private void asteapta() {
        opreste();
        indicator.activ = false;
        indicator.repaint();
        stare.setText("Astept 5 executii ale timerului periodic...");
        Timer pornit = new Timer("timer-conditionat", true);
        timer = pornit;
        pornit.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (timer != pornit || !periodic.isSarcinaIndeplinita()) return;
                    opreste();
                    indicator.activ = true;
                    indicator.repaint();
                    stare.setText("Declansat automat: pragul de 5 executii a fost atins.");
                });
            }
        }, 0, 300);
    }

    public void opreste() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private static class Indicator extends JPanel {
        private boolean activ;

        Indicator() {
            setPreferredSize(new Dimension(24, 24));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(activ ? new Color(0, 160, 60) : Color.LIGHT_GRAY);
            g.fillOval(2, 2, 20, 20);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(2, 2, 20, 20);
        }
    }
}
