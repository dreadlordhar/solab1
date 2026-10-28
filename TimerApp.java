import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
 
/** Sarcina 1 + 2: actiune care scrie un mesaj in jurnal (la interval / la un moment dat). */
class MesajTask extends TimerTask {
    private final String mesaj;
    private final JTextArea jurnal;
 
    MesajTask(String mesaj, JTextArea jurnal) {
        this.mesaj = mesaj;
        this.jurnal = jurnal;
    }
 
    @Override
    public void run() {
        Toolkit.getDefaultToolkit().beep();
        TimerApp.scrie(jurnal, mesaj);
    }
}
 
/** Sarcina 3: actiune periodica - creste un contor si o bara de progres la fiecare perioada. */
class ContorTask extends TimerTask {
    private final int maxim;
    private final JProgressBar bara;
    private final JLabel eticheta;
    private final JTextArea jurnal;
    private int valoare = 0;
 
    ContorTask(int maxim, JProgressBar bara, JLabel eticheta, JTextArea jurnal) {
        this.maxim = maxim;
        this.bara = bara;
        this.eticheta = eticheta;
        this.jurnal = jurnal;
    }
 
    @Override
    public void run() {
        if (valoare < maxim) {
            valoare++;
            final int v = valoare;
            SwingUtilities.invokeLater(() -> {
                bara.setValue(v);
                eticheta.setText("Contor: " + v + " / " + maxim);
            });
        } else {
            TimerApp.scrie(jurnal, "Timerul periodic s-a oprit (cancel) dupa " + maxim + " executii.");
            cancel();
        }
    }
}
 
public class TimerApp extends JFrame {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final int MAXIM = 10;
 
    final JTextArea jurnal = new JTextArea(10, 40);
    final JButton btnIntarziere = new JButton("Start (peste 5 secunde)");
    final JButton btnMoment = new JButton("Programeaza la ora:");
    final JButton btnPeriodic = new JButton("Start periodic (1 s)");
    final JButton btnStop = new JButton("Stop periodic");
    final JTextField campOra = new JTextField(8);
    final JProgressBar bara = new JProgressBar(0, MAXIM);
    final JLabel eticheta = new JLabel("Contor: 0 / " + MAXIM);
 
    private Timer timerIntarziere;
    private Timer timerMoment;
    private Timer timerPeriodic;
 
    public TimerApp() {
        super("Laborator 1 - Timere (Timer / TimerTask)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
 
        // implicit: ora curenta + 15 secunde
        campOra.setText(FORMAT.format(new Date(System.currentTimeMillis() + 15000)));
        bara.setStringPainted(true);
        jurnal.setEditable(false);
 
        JPanel panou = new JPanel(new GridLayout(3, 1, 6, 6));
        panou.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
 
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.setBorder(BorderFactory.createTitledBorder("1. Reactie dupa un interval de timp"));
        p1.add(btnIntarziere);
 
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.setBorder(BorderFactory.createTitledBorder("2. Reactie la un anumit moment (HH:mm:ss)"));
        p2.add(btnMoment);
        p2.add(campOra);
 
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p3.setBorder(BorderFactory.createTitledBorder("3. Reactie cu o perioada indicata"));
        p3.add(btnPeriodic);
        p3.add(btnStop);
        p3.add(bara);
        p3.add(eticheta);
 
        panou.add(p1);
        panou.add(p2);
        panou.add(p3);
        add(panou, BorderLayout.NORTH);
        add(new JScrollPane(jurnal), BorderLayout.CENTER);
 
        btnIntarziere.addActionListener(e -> porneste1());
        btnMoment.addActionListener(e -> porneste2());
        btnPeriodic.addActionListener(e -> porneste3());
        btnStop.addActionListener(e -> opreste3());
 
        setSize(680, 430);
        setLocationRelativeTo(null);
    }
 
    /** schedule(task, delay): o singura executie dupa 5 secunde. */
    private void porneste1() {
        timerIntarziere = new Timer();
        timerIntarziere.schedule(new MesajTask("Timer 1: au trecut 5 secunde.", jurnal), 5000);
        scrie(jurnal, "Timer 1 pornit (executie peste 5 secunde).");
    }
 
    /** schedule(task, Date): executie la ora introdusa de utilizator. */
    private void porneste2() {
        try {
            String[] p = campOra.getText().trim().split(":");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(p[0]));
            c.set(Calendar.MINUTE, Integer.parseInt(p[1]));
            c.set(Calendar.SECOND, p.length > 2 ? Integer.parseInt(p[2]) : 0);
            if (c.getTime().before(new Date())) {
                c.add(Calendar.DAY_OF_MONTH, 1); // ora a trecut -> maine
            }
            if (timerMoment != null) timerMoment.cancel();
            timerMoment = new Timer();
            timerMoment.schedule(new MesajTask("Timer 2: a sosit ora " + FORMAT.format(c.getTime()) + ".", jurnal),
                    c.getTime());
            scrie(jurnal, "Timer 2 programat pentru ora " + FORMAT.format(c.getTime()) + ".");
        } catch (RuntimeException ex) {
            scrie(jurnal, "Format invalid. Folositi HH:mm:ss (ex. 18:30:00).");
        }
    }
 
    /** scheduleAtFixedRate: executie repetata la fiecare secunda, pana la MAXIM. */
    private void porneste3() {
        opreste3();
        bara.setValue(0);
        eticheta.setText("Contor: 0 / " + MAXIM);
        timerPeriodic = new Timer();
        timerPeriodic.scheduleAtFixedRate(new ContorTask(MAXIM, bara, eticheta, jurnal), 0, 1000);
        scrie(jurnal, "Timer 3 pornit (perioada 1 secunda).");
    }
 
    /** cancel(): oprire fortata; un Timer anulat nu mai poate fi refolosit, deci se creeaza unul nou la start. */
    private void opreste3() {
        if (timerPeriodic != null) {
            timerPeriodic.cancel();
            timerPeriodic = null;
            scrie(jurnal, "Timer 3 oprit manual (cancel).");
        }
    }
 
    static void scrie(JTextArea jurnal, String text) {
        SwingUtilities.invokeLater(() ->
                jurnal.append("[" + FORMAT.format(new Date()) + "] " + text + "\n"));
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TimerApp().setVisible(true));
    }
}
