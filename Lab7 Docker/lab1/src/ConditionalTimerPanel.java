import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TIMER CONDITIONAT: NU are buton de Start.
 *
 * De cum apare pe ecran, porneste singur un Timer de "veghe" in fundal,
 * care verifica la fiecare 300ms daca sarcina 3 s-a indeplinit
 * (Task3Panel.isSarcinaIndeplinita()).
 *
 * Cat timp conditia e falsa, cerculetul ramane gri (asteapta).
 * In clipa in care conditia devine adevarata, timerul "se declanseaza"
 * singur: cerculetul devine verde si ramane asa (dovada declansarii).
 */
public class ConditionalTimerPanel extends JPanel {

    private final Task3Panel task3Panel; // referinta catre celalalt timer, pentru a-i verifica starea

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

        // Pornire automata, fara interventia utilizatorului:
        pornesteVeghea();
    }

    private void pornesteVeghea() {
        vegheTimer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (declansat) {
                    return; // deja s-a declansat o data, nu mai facem nimic
                }
                if (!task3Panel.isSarcinaIndeplinita()) {
                    return; // conditia inca nu e indeplinita -> continuam sa asteptam
                }

                // Conditia s-a indeplinit: ne declansam singuri, automat
                declansat = true;

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("DECLANSAT AUTOMAT!");
                    statusLabel.setForeground(new Color(200, 0, 0));
                    cerculet.aprinde();
                });

                vegheTimer.cancel(); // ne oprim, ne-am facut treaba
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