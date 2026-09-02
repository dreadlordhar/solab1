import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

public class TaimerMomentExact extends TimerTask {

    private static final SimpleDateFormat FORMAT_ORA = new SimpleDateFormat("HH:mm:ss.SSS");

    private final String eticheta;
    private final Date momentTinta;

    public TaimerMomentExact(String eticheta, Date momentTinta) {
        this.eticheta = eticheta;
        this.momentTinta = momentTinta;
    }

    @Override
    public void run() {
        System.out.println("[" + FORMAT_ORA.format(new Date()) + "] " + eticheta
                + " a reactionat la momentul de timp programat ("
                + FORMAT_ORA.format(momentTinta) + ").");
    }
}
