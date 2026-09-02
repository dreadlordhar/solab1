import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

/**
 * Aplicatie cu mai multe taimere, folosind clasele java.util.Timer si
 * java.util.TimerTask.
 *
 * Fiecare tip de taimer este implementat intr-un fisier separat:
 *   - TaimerInterval.java     -> reactioneaza dupa un anumit interval de timp
 *   - TaimerMomentExact.java  -> reactioneaza la un anumit moment de timp
 *   - TaimerPeriodic.java     -> reactioneaza periodic, cu o perioada indicata
 *
 * Intarzierile folosite mai jos sunt scurte si nu sunt secunde intregi,
 * pentru ca cele trei taimere sa reactioneze unul dupa altul, la intervale
 * scurte, usor de urmarit in consola.
 */
public class AplicatieTaimere {

    private static final SimpleDateFormat FORMAT_ORA = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Start aplicatie: " + FORMAT_ORA.format(new Date()));
        System.out.println("--------------------------------------------------");

        // 1) Taimer care reactioneaza dupa un ANUMIT INTERVAL de timp
        Timer taimerInterval = new Timer("TaimerInterval");
        long intarziereInterval = 700; // ms
        taimerInterval.schedule(new TaimerInterval("Taimer-1-Interval", intarziereInterval), intarziereInterval);
        System.out.println("[Taimer-1] Programat sa reactioneze peste " + intarziereInterval + " ms.");

        // 2) Taimer care reactioneaza la un ANUMIT MOMENT de timp
        Timer taimerMoment = new Timer("TaimerMomentExact");
        Date momentTinta = new Date(System.currentTimeMillis() + 1200); // peste 1200 ms de acum
        taimerMoment.schedule(new TaimerMomentExact("Taimer-2-MomentExact", momentTinta), momentTinta);
        System.out.println("[Taimer-2] Programat sa reactioneze la ora " + FORMAT_ORA.format(momentTinta));

        // 3) Taimer care reactioneaza PERIODIC, cu o perioada indicata
        Timer taimerPeriodic = new Timer("TaimerPeriodic");
        long intarziereInitiala = 300;  // ms, prima reactie
        long perioada = 650;            // ms, intre reactii succesive
        TaimerPeriodic taskPeriodic = new TaimerPeriodic("Taimer-3-Periodic", perioada);
        taimerPeriodic.scheduleAtFixedRate(taskPeriodic, intarziereInitiala, perioada);
        System.out.println("[Taimer-3] Programat sa reactioneze la fiecare " + perioada
                + " ms, incepand peste " + intarziereInitiala + " ms.");

        System.out.println("--------------------------------------------------");

        // Lasam aplicatia sa ruleze suficient timp cat toate cele 3 taimere
        // sa reactioneze, unul dupa altul, la intervale scurte.
        int durataRulareMs = 3000;
        Thread.sleep(durataRulareMs);

        taimerPeriodic.cancel();
        taimerInterval.cancel();
        taimerMoment.cancel();

        System.out.println("--------------------------------------------------");
        System.out.println("[Taimer-3] Oprit dupa " + durataRulareMs + " ms de rulare.");
        System.out.println("Sfarsit aplicatie: " + FORMAT_ORA.format(new Date()));
    }
}
