import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService {

    private final Timer timer = new Timer();
    private final List<TimerTask> scheduledTasks = new ArrayList<>();
    private TimerTask repeatingTask;

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

    public void cancelRepeating() {
        if (repeatingTask != null) {
            repeatingTask.cancel();
            repeatingTask = null;
        }
    }

    public void cancelScheduledTasks() {
        synchronized (scheduledTasks) {
            for (TimerTask task : scheduledTasks) {
                task.cancel();
            }
            scheduledTasks.clear();
        }
    }

    public void cancelAll() {
        cancelRepeating();
        cancelScheduledTasks();
    }
}
