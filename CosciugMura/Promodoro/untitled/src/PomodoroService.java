import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
public class PomodoroService {
private Timer timerPeriodic;
private Timer timerDelay;
private Timer timerOraFixa;

public interface TimerCallback{
    void onTick(int secondsLeft);
    void onDelayComplete(String msg);
    void onExactTimeReached(String msg);
}
public void startPomodoro(int totalSeconds, TimerCallback callback) {
        if (timerPeriodic != null) {
            timerPeriodic.cancel();
        }
}
