package com.lab.timermanager.service;

import com.lab.timermanager.model.TimerModel;
import com.lab.timermanager.model.TimerStatus;
import javafx.application.Platform;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviciul responsabil de planificarea efectiva a timerelor.
 * <p>
 * Aceasta este singura clasa care lucreaza direct cu {@link java.util.Timer} si
 * {@link java.util.TimerTask} - GUI-ul (controller/view) nu stie nimic despre
 * cum e implementata planificarea, ci doar cere startTimer/stopTimer.
 * <p>
 * Fiecare {@link TimerModel} pornit primeste propriul obiect Timer (fir de executie separat),
 * astfel incat mai multe timere pot rula independent si simultan.
 * <p>
 * Aceasta versiune implementeaza un singur mod de planificare - DELAY
 * (reactioneaza o singura data, dupa un interval de timp indicat de utilizator).
 * <p>
 * Pe langa timer-ele "de lucru" (create de utilizator), serviciul mai foloseste
 * un singur java.util.Timer suplimentar - "ticker"-ul - care ruleaza o data pe secunda
 * si actualizeaza pe GUI textul de tip "timp ramas".
 */
public class TimerService {

    /** Timer-ul Java (fir de executie) asociat fiecarui TimerModel activ, cheie = TimerModel.getId(). */
    private final Map<String, Timer> activeTimers = new ConcurrentHashMap<>();

    /** Momentul (epoch millis) urmatoarei executii, folosit pentru afisarea numaratorii inverse. */
    private final Map<String, Long> nextFireAtMillis = new ConcurrentHashMap<>();

    /** Modelele urmarite curent de ticker (adica active/RUNNING). */
    private final Set<TimerModel> trackedModels = ConcurrentHashMap.newKeySet();

    /** Singurul Timer folosit pentru a reimprospata interfata grafica, o data pe secunda. */
    private final Timer uiTicker = new Timer("ui-ticker", true);

    public TimerService() {
        // TimerTask care ruleaza la fiecare secunda si recalculeaza "timpul ramas" pentru
        // toate timerele active. Este o utilizare suplimentara, separata, a clasei Timer.
        uiTicker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshCountdowns();
            }
        }, 0, 1000);
    }

    /**
     * Porneste planificarea pentru un timer de tip DELAY.
     * Foloseste efectiv clasa Timer si o subclasa/instanta anonima de TimerTask,
     * exact ca in exemplele din indrumar.
     */
    public void startTimer(TimerModel model) {
        if (model.getStatus() == TimerStatus.RUNNING) {
            return;
        }

        // Pas 1: cream un fir de executie prin instantierea clasei Timer.
        Timer timer = new Timer("timer-" + model.getName(), true);
        activeTimers.put(model.getId(), timer);

        scheduleDelay(model, timer);

        model.setStatus(TimerStatus.RUNNING);
        trackedModels.add(model);
    }

    /** Reactioneaza dupa un anumit interval de timp (delay), o singura data. */
    private void scheduleDelay(TimerModel model, Timer timer) {
        long delayMs = model.getDelaySeconds() * 1000L;
        nextFireAtMillis.put(model.getId(), System.currentTimeMillis() + delayMs);

        // Pas 2: cream un obiect de tip actiune -> subclasa anonima de TimerTask
        // si suprascriem metoda run() cu actiunea planificata.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    model.setStatus(TimerStatus.COMPLETED);
                    model.setNextExecutionText("Finalizat");
                });
                trackedModels.remove(model);
            }
        };

        // Pas 3: planificam executia folosind metoda schedule(task, delay).
        timer.schedule(task, delayMs);
    }

    /**
     * Opreste fortat un timer, folosind metoda cancel() a clasei Timer.
     * Dupa oprire, obiectul Timer nu mai poate fi refolosit pentru planificare -
     * de aceea la un urmator Start se creeaza un Timer complet nou.
     */
    public void stopTimer(TimerModel model) {
        Timer timer = activeTimers.remove(model.getId());
        if (timer != null) {
            timer.cancel();
        }
        nextFireAtMillis.remove(model.getId());
        trackedModels.remove(model);

        model.setStatus(TimerStatus.STOPPED);
        model.setNextExecutionText("-");
    }

    /** Sterge complet un timer: il opreste (daca era activ) si elibereaza resursele lui. */
    public void deleteTimer(TimerModel model) {
        stopTimer(model);
    }

    /** Opreste toate timerele si ticker-ul de UI - apelata la inchiderea aplicatiei. */
    public void shutdownAll() {
        activeTimers.values().forEach(Timer::cancel);
        activeTimers.clear();
        trackedModels.clear();
        uiTicker.cancel();
    }

    /** Ruleaza pe firul ticker-ului: recalculeaza textul de countdown pentru fiecare model urmarit. */
    private void refreshCountdowns() {
        if (trackedModels.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();

        Platform.runLater(() -> {
            for (TimerModel model : trackedModels) {
                Long nextFire = nextFireAtMillis.get(model.getId());
                if (nextFire == null) {
                    continue;
                }
                long remainingMs = Math.max(0, nextFire - now);
                model.setNextExecutionText(formatDuration(remainingMs));
            }
        });
    }

    private String formatDuration(long millis) {
        long totalSeconds = millis / 1000;
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }
}
