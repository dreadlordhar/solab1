import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;

public class PomodoroService {
    private Timer timerPeriodic;
    private Timer timerDelay;
    private Timer timerOraFixa;

    public interface TimerCallback {
        void onTick(int secondsLeft);
        void onDelayComplete(String msg);
        void onExactTimeReached(String msg);
    }

    public void startPomodoro(int totalSeconds, TimerCallback callback) {
        if (timerPeriodic != null) {
            timerPeriodic.cancel();
        }
        timerPeriodic = new Timer();
        timerPeriodic.scheduleAtFixedRate(new TimerTask() {
            int timeRemaining = totalSeconds;

            @Override
            public void run() {
                if (timeRemaining >= 0) {
                    callback.onTick(timeRemaining);
                    timeRemaining--;
                } else {
                    timerPeriodic.cancel();
                }
            }
        }, 0, 1000);
    }

    public void scheduleNotificationDelay(int delaySeconds, TimerCallback callback) {
        if (timerDelay != null) {
            timerDelay.cancel();
        }

        timerDelay = new Timer();
        timerDelay.schedule(new TimerTask() {
            @Override
            public void run() {
                callback.onDelayComplete("Sesiunea de concentrare a fost configurata cu succes!");
            }
        }, delaySeconds * 1000L);
    }

    public void scheduleEndOfDayAlarm(Date targetTime, TimerCallback callback) {
        if (timerOraFixa != null) {
            timerOraFixa.cancel();
        }

        timerOraFixa = new Timer();
        timerOraFixa.schedule(new TimerTask() {
            @Override
            public void run() {
                callback.onExactTimeReached("Ora stabilita a sosit! Este timpul pentru pauza.");
            }
        }, targetTime);
    }

    public void stopAll() {
        if (timerPeriodic != null) timerPeriodic.cancel();
        if (timerDelay != null) timerDelay.cancel();
        if (timerOraFixa != null) timerOraFixa.cancel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PomodoroUI fereastra = new PomodoroUI();
            fereastra.setVisible(true);
        });
    }
}