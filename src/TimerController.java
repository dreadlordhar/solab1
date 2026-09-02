import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;

public class TimerController {

    private final TimerModel model;
    private final TimerPanel view;

    // Timerul java.util
    private final Timer timer;

    // Sarcina executată de timer
    private TimerTask timerTask;

    public TimerController(
            TimerModel model,
            TimerPanel view
    ) {

        this.model = model;
        this.view = view;

        // Creăm Timer-ul
        timer = new Timer();

        // Adăugăm acțiunile butoanelor
        view.getStartButton()
                .addActionListener(this::startTimer);

        view.getPauseButton()
                .addActionListener(this::pauseTimer);

        view.getResetButton()
                .addActionListener(this::resetTimer);
    }

    // START

    private void startTimer(ActionEvent e) {

        // Setăm timpul dacă timerul nu a fost pornit
        if (model.getRemainingSeconds() <= 0) {

            int minutes =
                    (Integer) view
                            .getMinutesSpinner()
                            .getValue();

            int seconds =
                    (Integer) view
                            .getSecondsSpinner()
                            .getValue();

            int totalSeconds =
                    minutes * 60 + seconds;

            // Verificăm timpul introdus
            if (totalSeconds <= 0) {

                view.showWarning(
                        "Setează un interval mai mare decât 0 secunde!",
                        "Invalid Time"
                );

                return;
            }

            model.setTime(
                    minutes,
                    seconds
            );
        }

        // Creăm sarcina executată de Timer
        timerTask = new TimerTask() {

            @Override
            public void run() {

                // Actualizăm interfața în thread-ul Swing
                SwingUtilities.invokeLater(() -> updateTimer());
            }
        };

        // Executăm după 1 secundă și apoi la fiecare 1 secundă
        timer.schedule(timerTask, 1000, 1000);

        view.getStartButton()
                .setEnabled(false);

        view.getPauseButton()
                .setEnabled(true);

        view.setSpinnersEnabled(false);

        view.setStatusText(
                "Mission in progress..."
        );
    }

    // ACTUALIZARE TIMER

    private void updateTimer() {

        // Verificăm dacă mai există timp
        if (!model.isFinished()) {

            // Scădem o secundă
            model.tick();

            // Actualizăm afișarea
            updateDisplay();

        } else {

            // Timpul a expirat
            finishTimer();
        }
    }

    // ACTUALIZARE AFIȘARE

    private void updateDisplay() {

        int minutes =
                model.getMinutes();

        int seconds =
                model.getSeconds();

        // Formatăm timpul MM:SS
        String time =
                String.format(
                        "%02d:%02d",
                        minutes,
                        seconds
                );

        view.setTimerText(time);

        // Actualizăm bara de progres
        view.setProgress(
                model.getProgress()
        );
    }

    // PAUZĂ

    private void pauseTimer(ActionEvent e) {

        // Oprim sarcina curentă
        if (timerTask != null) {
            timerTask.cancel();
        }

        view.getStartButton()
                .setEnabled(true);

        view.getPauseButton()
                .setEnabled(false);

        view.setStatusText(
                "⏸ Mission paused"
        );
    }

    // RESET

    private void resetTimer(ActionEvent e) {

        // Oprim sarcina curentă
        if (timerTask != null) {
            timerTask.cancel();
        }

        // Resetăm modelul
        model.reset();

        view.setTimerText("00:00");

        view.setProgress(0);

        view.setStatusText(
                "Ready for launch"
        );

        view.getStartButton()
                .setEnabled(true);

        view.getPauseButton()
                .setEnabled(false);

        view.setSpinnersEnabled(true);
    }

    // FINALIZARE TIMER

    private void finishTimer() {

        // Oprim sarcina
        if (timerTask != null) {
            timerTask.cancel();
        }

        view.getStartButton()
                .setEnabled(true);

        view.getPauseButton()
                .setEnabled(false);

        view.setSpinnersEnabled(true);

        view.setStatusText(
                "Mission completed!"
        );

        view.setProgress(100);

        // Afișăm mesajul final
        view.showMessage(
                "🚀 Timpul a expirat!\n"
                        + "Nava spațială a decolat.",
                "Mission Complete"
        );
    }
}