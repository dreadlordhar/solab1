package circlecatcher.timers;

import java.util.*;

// Just to use inside Timer lambdas via Task.set(...)
public class FunctionalTimerTask extends TimerTask {

    Runnable task;

    public FunctionalTimerTask(Runnable task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.run();
    }
}