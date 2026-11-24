import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SARCINA 3: Timer care reactioneaza cu o perioadă indicata de utilizator.
 *
 * Folosim java.util.Timer + java.util.TimerTask:
 * timer.scheduleAtFixedRate(task, delayInitial, perioada) face ca task-ul
 * sa se repete la interval fix (perioada), in milisecunde.
 * Dupa un anumit numar de declansari (PRAG), consideram sarcina indeplinita.
 * Alte clase (ex: ConditionalTimerPanel) pot verifica acest lucru prin
 * metoda isSarcinaIndeplinita().
 */
public class Task3Panel extends JPanel {

    private static final int PRAG = 5;

    private final JTextField perioadaField;
    private final JLabel statusLabel;
    private final JLabel counterLabel;
    private final JButton startButton;
    private final JButton stopButton;
  

    private Timer timer;
    private int contor = 0;

    private volatile boolean sarcinaIndeplinita = false;

    public Task3Panel() {
        setBorder(BorderFactory.createTitledBorder("Sarcina 3: Timer periodic"));
        setLayout(new GridLayout(0, 1, 5, 5));

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Perioada (ms):"));
        perioadaField = new JTextField("2000", 6);
        inputPanel.add(perioadaField);
        add(inputPanel);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        add(buttonPanel);

        counterLabel = new JLabel("Declansari: 0 / " + PRAG);
        add(counterLabel);

        statusLabel = new JLabel("Stare: neinceput");
        add(statusLabel);

        startButton.addActionListener(e -> pornesteTimer());
        stopButton.addActionListener(e -> opresteTimer());
    }

    private void pornesteTimer() {
        int perioada;
        try {
            perioada = Integer.parseInt(perioadaField.getText().trim());
            if (perioada <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Introduceti o perioada valida (numar intreg > 0) in milisecunde.");
            return;
        }

        contor = 0;
        sarcinaIndeplinita = false;
        counterLabel.setText("Declansari: 0 / " + PRAG);
        statusLabel.setText("Stare: ruleaza...");

        timer = new Timer(true); // true = daemon, nu blocheaza inchiderea aplicatiei

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                contor++;
                boolean tocmaiIndeplinita = (contor == PRAG);
                if (tocmaiIndeplinita) {
                    sarcinaIndeplinita = true;
                }

                // Actualizarile de UI trebuie facute pe Event Dispatch Thread (EDT)
                SwingUtilities.invokeLater(() -> {
                    counterLabel.setText("Declansari: " + contor + " / " + PRAG);
                    if (sarcinaIndeplinita) {
                        statusLabel.setText("Stare: SARCINA INDEPLINITA!");
                        statusLabel.setForeground(new Color(0, 128, 0));
                    } else {
                        statusLabel.setText("Stare: ruleaza... (" + contor + ")");
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(task, 0, perioada);

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        perioadaField.setEnabled(false);
    }

    private void opresteTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        statusLabel.setText("Stare: oprit");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        perioadaField.setEnabled(true);
    }

    /*
     * Metoda folosita de alte clase pentru a verifica daca sarcina 3 s-a indeplinit
     */

    public boolean isSarcinaIndeplinita() {
        return sarcinaIndeplinita;
    }
}