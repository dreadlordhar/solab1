import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService {

    // Motorul Java care executa toate sarcinile programate.
    private final Timer timer = new Timer();

    // Lista programarilor unice active pentru Timer 2.
    private final List<TimerTask> scheduledTasks = new ArrayList<>();

    // Sarcina repetitiva activa pentru countdown-ul Timer 1.
    private TimerTask repeatingTask;

    // Porneste o sarcina dupa delay si o repeta la fiecare period milisecunde.
    public void scheduleRepeating(long delay, long period, Runnable action) {
        cancelRepeating();
        repeatingTask = new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        };
        timer.schedule(repeatingTask, delay, period);
    }

    // Programeaza o sarcina care se executa o singura data la data indicata.
    public void scheduleAt(Date target, Runnable action) {
        final TimerTask[] taskReference = new TimerTask[1];
        taskReference[0] = new TimerTask() {
            @Override
            public void run() {
                synchronized (scheduledTasks) {
                    scheduledTasks.remove(taskReference[0]);
                }
                action.run();
            }
        };
        synchronized (scheduledTasks) {
            scheduledTasks.add(taskReference[0]);
        }
        timer.schedule(taskReference[0], target);
    }

    // Opreste countdown-ul repetitiv curent.
    public void cancelRepeating() {
        if (repeatingTask != null) {
            repeatingTask.cancel();
            repeatingTask = null;
        }
    }

    // Opreste toate programarile unice active din Timer 2.
    public void cancelScheduledTasks() {
        synchronized (scheduledTasks) {
            for (TimerTask task : scheduledTasks) {
                task.cancel();
            }
            scheduledTasks.clear();
        }
    }

    // Opreste toate tipurile de timer gestionate de acest serviciu.
    public void cancelAll() {
        cancelRepeating();
        cancelScheduledTasks();
    }
}
