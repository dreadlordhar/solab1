import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class TimerApp {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter
            .ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private TimerApp() {
    }

    public static void main(String[] args) throws InterruptedException {
        Timer periodicTimer = new Timer("periodic-timer", true);
        Timer delayTimer = new Timer("delay-timer", true);
        Timer clockTimer = new Timer("clock-timer", true);
        CountDownLatch oneShotTasks = new CountDownLatch(2);

        log("setup", "Au fost pornite trei obiecte Timer.");

        periodicTimer.scheduleAtFixedRate(new TimerTask() {
            private int tick;

            @Override
            public void run() {
                tick++;
                log("period", "execuția " + tick + " la interval de 1 secundă");
                if (tick >= 4) {
                    cancel();
                    log("period", "planificarea periodică s-a încheiat");
                }
            }
        }, 0L, 1_000L);

        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                log("delay", "acțiune declanșată după 2,5 secunde");
                oneShotTasks.countDown();
            }
        }, 2_500L);

        Instant target = Instant.now().plusSeconds(5);
        Date targetDate = Date.from(target);
        clockTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                log("date", "acțiune la momentul planificat " + CLOCK.format(target));
                oneShotTasks.countDown();
            }
        }, targetDate);

        boolean completed = oneShotTasks.await(8, TimeUnit.SECONDS);
        periodicTimer.cancel();
        delayTimer.cancel();
        clockTimer.cancel();

        if (!completed) {
            throw new IllegalStateException("Acțiunile programate nu s-au încheiat la timp.");
        }
        log("done", "Toate planificările au fost executate cu succes.");
    }

    private static void log(String source, String message) {
        System.out.printf("[%s] [%-6s] %s%n", CLOCK.format(Instant.now()), source, message);
    }
}
