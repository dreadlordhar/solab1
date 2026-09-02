public class TimerModel {

    // Timpul total în secunde
    private int totalSeconds;

    // Timpul rămas în secunde
    private int remainingSeconds;

    // Setăm timpul timerului
    public void setTime(int minutes, int seconds) {

        totalSeconds = minutes * 60 + seconds;
        remainingSeconds = totalSeconds;
    }

    // Scădem o secundă
    public void tick() {

        if (remainingSeconds > 0) {
            remainingSeconds--;
        }
    }

    // Verificăm dacă timpul a expirat
    public boolean isFinished() {

        return remainingSeconds <= 0;
    }

    // Returnăm timpul rămas
    public int getRemainingSeconds() {

        return remainingSeconds;
    }

    // Returnăm timpul total
    public int getTotalSeconds() {

        return totalSeconds;
    }

    // Returnăm minutele
    public int getMinutes() {

        return remainingSeconds / 60;
    }

    // Returnăm secundele
    public int getSeconds() {

        return remainingSeconds % 60;
    }

    // Calculăm procentul de progres
    public int getProgress() {

        if (totalSeconds <= 0) {
            return 0;
        }

        return (int) (
                ((double) (totalSeconds - remainingSeconds)
                        / totalSeconds) * 100
        );
    }

    // Resetăm timerul
    public void reset() {

        totalSeconds = 0;
        remainingSeconds = 0;
    }
}