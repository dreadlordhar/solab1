package com.lab.timermanager.service;

import com.lab.timermanager.model.TimerModel;
import com.lab.timermanager.model.TimerStatus;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
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
 * Pe langa timer-ele "de lucru" (create de utilizator), serviciul mai foloseste
 * un singur java.util.Timer suplimentar - "ticker"-ul - care ruleaza o data pe secunda
 * si actualizeaza pe GUI textul de tip "timp ramas / urmatoarea executie".
 */
public class TimerService {

    /** Timer-ul Java (fir de executie) asociat fiecarui TimerModel activ, cheie = TimerModel.getId(). */
    private final Map<String, Timer> activeTimers = new ConcurrentHashMap<>();

    /** Momentul (epoch millis) urmatoarei executii, folosit pentru afisarea numaratorii inverse. */
    private final Map<String, Long> nextFireAtMillis = new ConcurrentHashMap<>();

    /** Perioada (ms) pentru timerele PERIODIC - folosita doar pentru afisare pe card. */
    private final Map<String, Long> periodMillisById = new ConcurrentHashMap<>();

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
     * Porneste planificarea pentru un timer, in functie de tipul lui.
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

        switch (model.getType()) {
            case DELAY -> scheduleDelay(model, timer);
            case SPECIFIC_TIME -> scheduleSpecificTime(model, timer);
            case PERIODIC -> schedulePeriodic(model, timer);
        }

        model.setStatus(TimerStatus.RUNNING);
        trackedModels.add(model);
    }

    /** Tip 1: reactioneaza dupa un anumit interval de timp (delay), o singura data. */
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
                    model.incrementExecutionCount();
                });
                trackedModels.remove(model);
            }
        };

        // Pas 3: planificam la executie folosind metoda schedule(task, delay).
        timer.schedule(task, delayMs);
    }

    /** Tip 2: reactioneaza la o data/ora exacta aleasa de utilizator. */
    private void scheduleSpecificTime(TimerModel model, Timer timer) {
        LocalDateTime target = model.getSpecificDateTime();
        Date fireDate = Date.from(target.atZone(ZoneId.systemDefault()).toInstant());

        // Daca ora aleasa a trecut deja azi, o programam pentru aceeasi ora, a doua zi
        // (acelasi principiu ca in Exemplul 1 din indrumar, cu obiectul Calendar).
        if (fireDate.before(new Date())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fireDate);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            fireDate = cal.getTime();
        }

        nextFireAtMillis.put(model.getId(), fireDate.getTime());

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    model.setStatus(TimerStatus.COMPLETED);
                    model.setNextExecutionText("Finalizat");
                    model.incrementExecutionCount();
                });
                trackedModels.remove(model);
            }
        };

        // Varianta schedule(TimerTask task, Date time) - executie la un moment exact.
        timer.schedule(task, fireDate);
    }

    /** Tip 3: reactioneaza repetat, cu o perioada indicata de utilizator. */
    private void schedulePeriodic(TimerModel model, Timer timer) {
        long delayMs = 0L; // porneste imediat, apoi se repeta
        long periodMs = model.getPeriodSeconds() * 1000L;
        periodMillisById.put(model.getId(), periodMs);
        nextFireAtMillis.put(model.getId(), System.currentTimeMillis() + periodMs);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                nextFireAtMillis.put(model.getId(), System.currentTimeMillis() + periodMs);
                Platform.runLater(model::incrementExecutionCount);
            }
        };

        // scheduleAtFixedRate -> numar fix de executii pe unitatea de timp,
        // recomandat pentru actiuni repetate (ex: MP3 Player / Exemplul 3 din indrumar).
        timer.scheduleAtFixedRate(task, delayMs, periodMs);
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
        periodMillisById.remove(model.getId());
        trackedModels.remove(model);

        model.setStatus(TimerStatus.STOPPED);
        model.setNextExecutionText("-");
    }

    /** Sterge complet un timer: il opreste (daca era activ) si elibereaza resursele lui. */
    public void deleteTimer(TimerModel model) {
        stopTimer(model);
    }

    /** Oprește toate timerele si ticker-ul de UI - apelata la inchiderea aplicatiei. */
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
                String text = formatDuration(remainingMs);
                if (model.getType() == com.lab.timermanager.model.TimerType.PERIODIC) {
                    text += "  (executii: " + model.getExecutionCount() + ")";
                }
                model.setNextExecutionText(text);
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
