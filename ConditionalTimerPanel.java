import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;


public class ConditionalTimerPanel extends JPanel {

    private final Task3Panel task3Panel; 

    private final JLabel statusLabel;
    private final CircleIndicator cerculet;

    private Timer vegheTimer;
    private boolean declansat = false;

    public ConditionalTimerPanel(Task3Panel task3Panel) {
        this.task3Panel = task3Panel;

        setBorder(BorderFactory.createTitledBorder("Timer conditionat"));
        setLayout(new GridLayout(0, 1, 5, 5));

        statusLabel = new JLabel("In asteptare...");
        add(statusLabel);

        JPanel cerculetWrapper = new JPanel();
        cerculetWrapper.add(new JLabel("Semnal declansare:"));
        cerculet = new CircleIndicator();
        cerculetWrapper.add(cerculet);
        add(cerculetWrapper);

        pornesteVeghea();
    }

    private void pornesteVeghea() {
        vegheTimer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (declansat) {
                    return; 
                }
                if (!task3Panel.isSarcinaIndeplinita()) {
                    return; 
                }

                declansat = true;

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("DECLANSAT AUTOMAT!");
                    statusLabel.setForeground(new Color(200, 0, 0));
                    cerculet.aprinde();
                });

                vegheTimer.cancel(); 
            }
        };

        vegheTimer.scheduleAtFixedRate(task, 0, 300);
    }

    private static class CircleIndicator extends JPanel {
        private Color culoare = Color.LIGHT_GRAY;

        CircleIndicator() {
            setPreferredSize(new Dimension(24, 24));
        }

        void aprinde() {
            culoare = new Color(0, 200, 0);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(culoare);
            g.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
        }
    }
}