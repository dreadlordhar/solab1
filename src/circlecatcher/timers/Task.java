package circlecatcher.timers;

import java.util.*;

// Just to use inside Timer lambdas via Task.set(...)
public class Task {
    public static TimerTask set(Runnable run) {
        return new FunctionalTimerTask(run);
    }
}