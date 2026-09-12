import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.Date;

public class TimerController {

        private final TimerModel model;
        private final TimerPanel view;

        private final TimerService timerService;

        public TimerController(
                        TimerModel model,
                        TimerPanel view) {

                this.model = model;
                this.view = view;

                timerService = new TimerService();

                // Adăugăm acțiunile butoanelor
                view.getStartButton()
                                .addActionListener(this::startTimer);

                view.getPauseButton()
                                .addActionListener(this::pauseTimer);

                view.getResetButton()
                                .addActionListener(this::resetTimer);

                view.switchTimerPage(e -> switchTimer());
                view.onDeleteHistory(e -> view.deleteSelectedHistoryTime());
        }

        // START

        private void startTimer(ActionEvent e) {

                if (view.isExactTimeVisible()) {
                        startExactTimeTimer();
                        return;
                }

                // Setăm timpul dacă timerul nu a fost pornit
                if (model.getRemainingSeconds() <= 0) {

                        int minutes = (Integer) view
                                        .getMinutesSpinner()
                                        .getValue();

                        int seconds = (Integer) view
                                        .getSecondsSpinner()
                                        .getValue();

                        int totalSeconds = minutes * 60 + seconds;

                        // Verificăm timpul introdus
                        if (totalSeconds <= 0) {

                                view.showWarning(
                                                "Setează un interval mai mare decât 0 secunde!",
                                                "Invalid Time");

                                return;
                        }

                        model.setTime(
                                        minutes,
                                        seconds);
                }

                // Creăm sarcina executată de Timer
                // Executăm după 1 secundă și apoi la fiecare 1 secundă
                timerService.scheduleRepeating(
                                1000,
                                1000,
                                () -> SwingUtilities.invokeLater(this::updateTimer));

                view.getStartButton()
                                .setEnabled(false);

                view.getPauseButton()
                                .setEnabled(true);

                view.setSpinnersEnabled(false);

                view.setStatusText(
                                "Mission in progress...");
        }

        private void startExactTimeTimer() {

                Calendar target = Calendar.getInstance();
                target.set(Calendar.HOUR_OF_DAY, view.getTargetHour());
                target.set(Calendar.MINUTE, view.getTargetMinute());
                target.set(Calendar.SECOND, view.getTargetSecond());
                target.set(Calendar.MILLISECOND, 0);

                if (!target.getTime().after(new Date())) {
                        view.showWarning(
                                        "Ora selectată a trecut. Alege o oră viitoare.",
                                        "Ora invalidă");
                        return;
                }
                timerService.scheduleAt(
                                target.getTime(),
                                () -> SwingUtilities.invokeLater(this::finishExactTimer));

                view.addHistoryEntry(view.getTargetHour(), view.getTargetMinute(), view.getTargetSecond());

                view.getStartButton().setEnabled(true);
                view.setExactTimeSpinnersEnabled(true);
                view.setStatusText("Programarea a fost adăugată. Poți adăuga încă una.");
        }

        private void finishExactTimer() {
                view.setStatusText("O programare a fost declanșată.");
                view.showMessage(
                                "🚀 A sosit ora programată!\nNava spațială a decolat.",
                                "Mission Complete");
        }

        private void switchTimer() {
                boolean showExactTime = !view.isExactTimeVisible();
                timerService.cancelAll();
                view.animateToTimer(showExactTime);
                view.setPauseButtonVisible(!showExactTime);
                view.getStartButton().setEnabled(true);
                view.getPauseButton().setEnabled(false);
                view.setSpinnersEnabled(true);
                view.setExactTimeSpinnersEnabled(true);
                view.setStatusText(showExactTime
                                ? "Setează ora exactă de lansare"
                                : "Ready for launch");
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

                int minutes = model.getMinutes();

                int seconds = model.getSeconds();

                // Formatăm timpul MM:SS
                String time = String.format(
                                "%02d:%02d",
                                minutes,
                                seconds);

                view.setTimerText(time);

                // Actualizăm bara de progres
                view.setProgress(
                                model.getProgress());
        }

        // PAUZĂ

        private void pauseTimer(ActionEvent e) {

                // Oprim sarcina curentă
                timerService.cancelRepeating();

                view.getStartButton()
                                .setEnabled(true);

                view.getPauseButton()
                                .setEnabled(false);

                view.setStatusText(
                                "⏸ Mission paused");
        }

        // RESET

        private void resetTimer(ActionEvent e) {

                // Oprim sarcina curentă
                timerService.cancelAll();

                // Resetăm modelul
                model.reset();
                view.resetExactTimeToCurrent();

                view.setTimerText("00:00");

                view.setProgress(0);

                view.setStatusText(
                                "Ready for launch");

                view.getStartButton()
                                .setEnabled(true);

                view.getPauseButton()
                                .setEnabled(false);

                view.setSpinnersEnabled(true);
                view.setExactTimeSpinnersEnabled(true);
        }

        // FINALIZARE TIMER

        private void finishTimer() {

                // Oprim sarcina
                timerService.cancelRepeating();

                view.getStartButton()
                                .setEnabled(true);

                view.getPauseButton()
                                .setEnabled(false);

                view.setSpinnersEnabled(true);

                view.setStatusText(
                                "Mission completed!");

                view.setProgress(100);

                // Afișăm mesajul final
                view.showMessage(
                                "🚀 Timpul a expirat!\n"
                                                + "Nava spațială a decolat.",
                                "Mission Complete");
        }
}