import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

public class TaimerPeriodic extends TimerTask {

    private static final SimpleDateFormat FORMAT_ORA = new SimpleDateFormat("HH:mm:ss.SSS");

    private final String eticheta;
    private final long perioadaMs;
    private int numarExecutii = 0;

    public TaimerPeriodic(String eticheta, long perioadaMs) {
        this.eticheta = eticheta;
        this.perioadaMs = perioadaMs;
    }

    @Override
    public void run() {
        numarExecutii++;
        System.out.println("[" + FORMAT_ORA.format(new Date()) + "] " + eticheta
                + " a reactionat (executia nr. " + numarExecutii
                + ", perioada = " + perioadaMs + " ms).");
    }
}
