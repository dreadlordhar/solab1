import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

/**
 * Taimer de tip 1: reactioneaza o singura data, dupa un anumit INTERVAL
 * de timp (intarziere) de la momentul programarii.
 *
 * Se foloseste impreuna cu: new Timer().schedule(new TaimerInterval(...), intarziere);
 */
public class TaimerInterval extends TimerTask {

    private static final SimpleDateFormat FORMAT_ORA = new SimpleDateFormat("HH:mm:ss.SSS");

    private final String eticheta;
    private final long intarziereMs;

    public TaimerInterval(String eticheta, long intarziereMs) {
        this.eticheta = eticheta;
        this.intarziereMs = intarziereMs;
    }

    @Override
    public void run() {
        System.out.println("[" + FORMAT_ORA.format(new Date()) + "] " + eticheta
                + " a reactionat dupa intervalul programat de " + intarziereMs + " ms.");
    }
}
