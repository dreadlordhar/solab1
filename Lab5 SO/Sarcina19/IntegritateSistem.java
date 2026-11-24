// sarcina 19: verificarea periodica a integritatii fisierelor
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class IntegritateSistem {
    // folderul de lucru; celelalte cai pornesc de aici
    static Path BAZA = Paths.get(System.getProperty("user.home"), "SO_Lab5");
    // fisierele supravegheate
    static Path MONITORIZAT;
    // copia buna, folosita la restaurare
    static Path REFERINTA;
    // copii vechi, cu data in numele folderului
    static Path ARHIVA;
    static Path JURNAL;

    static {
        folosesteBaza(BAZA);
    }

    // muta toate caile intr-un alt folder de lucru (folosit de meniul din main)
    static void folosesteBaza(Path baza) {
        BAZA = baza.toAbsolutePath().normalize();
        MONITORIZAT = BAZA.resolve("monitorizat");
        REFERINTA = BAZA.resolve("backup").resolve("referinta");
        ARHIVA = BAZA.resolve("backup").resolve("arhiva");
        JURNAL = BAZA.resolve("jurnal.log");
        cicluri = 0;
    }

    static final DateTimeFormatter FMT_JURNAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static final DateTimeFormatter FMT_FOLDER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // lacat comun: un singur fir lucreaza cu fisierele odata
    static final Object BLOCARE = new Object();

    // la cate verificari se face un backup automat
    static final int CICLURI_PANA_LA_BACKUP_AUTOMAT = 6;

    // firul din fundal pentru verificarea periodica
    static ScheduledExecutorService planificator;
    static int cicluri = 0;

    // false = mesajele "ok" ajung doar in fisier, nu pe ecran
    static volatile boolean OK_PERIODIC_IN_CONSOLA = true;

    static void jurnal(String nivel, String mesaj) {
        jurnal(nivel, mesaj, true);
    }

    // scrie in jurnal.log cu data si ora; optional si pe ecran
    static synchronized void jurnal(String nivel, String mesaj, boolean peEcran) {
        String linie = LocalDateTime.now().format(FMT_JURNAL) + " [" + nivel + "] " + mesaj;
        if (peEcran)
            System.out.println(linie);
        try {
            Files.createDirectories(BAZA);
            Files.writeString(JURNAL, linie + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println("Nu s-a putut scrie in jurnal: " + e.getMessage());
        }
    }

    // amprenta fisierului; daca continutul se schimba, se schimba si amprenta
    static String sha256(Path fisier) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (var in = Files.newInputStream(fisier)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0)
                md.update(buf, 0, n);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : md.digest())
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // refuza caile care ies din folderul de lucru (../)
    static Path sigur(Path radacina, Path relativ) {
        Path p = radacina.resolve(relativ).normalize();
        if (!p.startsWith(radacina))
            throw new SecurityException("Cale in afara folderului de lucru: " + relativ);
        return p;
    }

    // toate fisierele dintr-un folder, ca cai relative
    static List<Path> listaFisiere(Path radacina) throws Exception {
        if (!Files.exists(radacina))
            return new ArrayList<>();
        try (Stream<Path> s = Files.walk(radacina)) {
            return s.filter(p -> Files.isRegularFile(p) && !Files.isSymbolicLink(p))
                    .map(radacina::relativize).sorted().collect(Collectors.toList());
        }
    }

    // copiaza un fisier pastrand calea relativa
    static void copiaza(Path sursaRad, Path destRad, Path relativ) throws Exception {
        Path sursa = sigur(sursaRad, relativ);
        Path dest = sigur(destRad, relativ);
        Files.createDirectories(dest.getParent());
        Files.copy(sursa, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    static void initializare() throws Exception {
        initializare(true);
    }

    // creeaza folderele; cu creeazaDemo adauga si fisiere de test
    static void initializare(boolean creeazaDemo) throws Exception {
        Files.createDirectories(MONITORIZAT);
        Files.createDirectories(REFERINTA);
        Files.createDirectories(ARHIVA);
        if (creeazaDemo && listaFisiere(MONITORIZAT).isEmpty()) {
            Files.writeString(MONITORIZAT.resolve("config.ini"), "[sistem]\nmod=normal\nversiune=1.0\n");
            Files.writeString(MONITORIZAT.resolve("utilizatori.dat"), "admin;user1;user2\n");
            Files.writeString(MONITORIZAT.resolve("setari_retea.cfg"), "dhcp=true\ndns=8.8.8.8\n");
            jurnal("INFO", "Au fost create fisierele demonstrative in " + MONITORIZAT);
        }
        if (listaFisiere(REFERINTA).isEmpty()) {
            backupManual(false);
        }
        jurnal("INFO", "Initializare finalizata. Folder de lucru: " + BAZA);
    }

    // starea curenta devine noua referinta
    static void backupManual(boolean cereConfirmare) throws Exception {
        synchronized (BLOCARE) {
            if (cereConfirmare) {
                int probleme = verifica(false, false);
                if (probleme > 0) {
                    System.out.println("ATENTIE: " + probleme + " fisier(e) difera de referinta actuala. "
                            + "Backup-ul manual va inlocui referinta cu starea CURENTA.");
                    if (!confirma("Continuati? (d/n): ")) {
                        jurnal("INFO", "Backup manual anulat de utilizator.");
                        return;
                    }
                }
            }
            int nr = 0;
            for (Path rel : listaFisiere(MONITORIZAT)) {
                copiaza(MONITORIZAT, REFERINTA, rel);
                nr++;
            }
            jurnal("BACKUP", "Backup manual: " + nr + " fisier(e) copiate in referinta.");
            instantaneu("manual");
        }
    }

    // copie cu data in nume; nu schimba referinta
    static void instantaneu(String tip) throws Exception {
        Path dest = ARHIVA.resolve(LocalDateTime.now().format(FMT_FOLDER) + "_" + tip);
        int nr = 0;
        for (Path rel : listaFisiere(MONITORIZAT)) {
            copiaza(MONITORIZAT, dest, rel);
            nr++;
        }
        jurnal("BACKUP", "Instantaneu (" + tip + "): " + nr + " fisier(e) -> " + dest.getFileName());
    }

    // compara amprentele cu referinta: lipsa = sters, amprenta diferita = modificat
    static int verifica(boolean restaurare, boolean scrieInJurnal) throws Exception {
        synchronized (BLOCARE) {
            int probleme = 0;
            List<Path> dinReferinta = listaFisiere(REFERINTA);
            for (Path rel : dinReferinta) {
                Path original = sigur(MONITORIZAT, rel);
                Path copie = sigur(REFERINTA, rel);
                String stare = null;
                if (!Files.exists(original)) {
                    stare = "STERS";
                } else if (!sha256(original).equals(sha256(copie))) {
                    stare = "MODIFICAT";
                }
                if (stare == null)
                    continue;
                probleme++;
                if (scrieInJurnal) {
                    jurnal("DETERIORAT", rel + " - " + stare + " (detectat la "
                            + LocalDateTime.now().format(FMT_JURNAL) + ")");
                }
                if (restaurare)
                    restaureaza(rel, copie, original);
            }
            if (scrieInJurnal) {
                for (Path rel : listaFisiere(MONITORIZAT)) {
                    if (!dinReferinta.contains(rel)) {
                        jurnal("INFO", rel + " - fisier nou, absent din backup (nu este modificat/sters).");
                    }
                }
            }
            return probleme;
        }
    }

    // copiaza fisierul din referinta si verifica amprenta
    static void restaureaza(Path rel, Path copie, Path original) {
        try {
            Files.createDirectories(original.getParent());
            Files.copy(copie, original, StandardCopyOption.REPLACE_EXISTING);
            if (sha256(original).equals(sha256(copie))) {
                jurnal("RESTAURAT", rel + " restaurat din backup.");
            } else {
                jurnal("EROARE", rel + " - restaurarea nu a trecut verificarea hash.");
            }
        } catch (Exception e) {
            jurnal("EROARE", rel + " - restaurare esuata: " + e.getMessage());
        }
    }

    static void pornesteMonitorizarea(int secunde) {
        pornesteMonitorizarea(secunde, 0);
    }

    // verifica la fiecare n secunde si restaureaza ce s-a stricat
    static void pornesteMonitorizarea(int secunde, int intarziereInitiala) {
        if (planificator != null && !planificator.isShutdown()) {
            System.out.println("Monitorizarea ruleaza deja.");
            return;
        }
        planificator = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "monitor-integritate");
            // firul se opreste odata cu programul
            t.setDaemon(true);
            return t;
        });
        planificator.scheduleWithFixedDelay(() -> {
            try {
                cicluri++;
                int probleme = verifica(true, true);
                if (probleme == 0)
                    jurnal("OK", "Verificare periodica: toate fisierele sunt intacte.", OK_PERIODIC_IN_CONSOLA);
                if (cicluri % CICLURI_PANA_LA_BACKUP_AUTOMAT == 0)
                    instantaneu("automat");
            } catch (Exception e) {
                jurnal("EROARE", "Verificare periodica esuata: " + e.getMessage());
            }
        }, intarziereInitiala, secunde, TimeUnit.SECONDS);
        jurnal("INFO", "Monitorizare pornita (interval " + secunde + " s; backup automat la fiecare "
                + CICLURI_PANA_LA_BACKUP_AUTOMAT + " cicluri).");
    }

    static void opresteMonitorizarea() {
        if (planificator != null) {
            planificator.shutdownNow();
            planificator = null;
            jurnal("INFO", "Monitorizare oprita.");
        } else {
            System.out.println("Monitorizarea nu ruleaza.");
        }
    }

    static boolean esteWindows() {
        return System.getProperty("os.name", "").toLowerCase().startsWith("windows");
    }

    // sfc = utilitar windows care verifica fisierele de sistem
    static void ruleazaSfc(String argument) {
        if (!esteWindows()) {
            System.out.println("sfc este disponibil doar pe Windows.");
            return;
        }
        try {
            String radacina = System.getenv("SystemRoot") != null ? System.getenv("SystemRoot") : "C:\\Windows";
            Path sfc = Paths.get(radacina, "System32", "sfc.exe");
            jurnal("INFO", "Ruleaza: sfc " + argument + " (poate dura cateva minute)...");
            Process p = new ProcessBuilder(sfc.toString(), argument).redirectErrorStream(true).start();
            byte[] out = p.getInputStream().readAllBytes();
            int cod = p.waitFor();
            // sfc scrie adesea in utf-16, deci verificam codarea
            String text = (out.length > 1 && out[1] == 0)
                    ? new String(out, StandardCharsets.UTF_16LE)
                    : new String(out, Charset.defaultCharset());
            text = text.replace("\0", "").trim();
            System.out.println(text);
            jurnal("SFC", "sfc " + argument + " finalizat, cod de iesire = " + cod
                    + " (0 = fara incalcari / rezultat conform; detalii: C:\\Windows\\Logs\\CBS\\CBS.log)");
        } catch (Exception e) {
            jurnal("EROARE", "sfc nu a putut fi rulat: " + e.getMessage());
        }
    }

    // strica fisiere de test ca sa se vada detectarea si restaurarea
    static void simuleazaDeteriorare() throws Exception {
        Path f1 = sigur(MONITORIZAT, Paths.get("config.ini"));
        Path f2 = sigur(MONITORIZAT, Paths.get("utilizatori.dat"));
        synchronized (BLOCARE) {
            if (Files.exists(f1))
                Files.writeString(f1, "mod=CORUPT\n", StandardOpenOption.APPEND);
            if (Files.exists(f2))
                Files.delete(f2);
        }
        System.out.println("Simulat: config.ini modificat si utilizatori.dat sters (doar in " + MONITORIZAT + ").");
    }

    // citirea comenzilor din consola
    static Scanner scanner = new Scanner(System.in);

    // intreaba si accepta "d" sau "da"
    static boolean confirma(String intrebare) {
        System.out.print(intrebare);
        String r = scanner.nextLine().trim().toLowerCase();
        return r.equals("d") || r.equals("da");
    }

    static void afiseazaJurnal() throws Exception {
        if (!Files.exists(JURNAL)) {
            System.out.println("Jurnalul este gol.");
            return;
        }
        Files.readAllLines(JURNAL, StandardCharsets.UTF_8).forEach(System.out::println);
    }

    static void meniu() {
        System.out.println("\n===== Sarcina 19: verificare integritate fisiere =====");
        System.out.println("1. Initializare (folder de lucru + fisiere demo + backup initial)");
        System.out.println("2. Backup manual");
        System.out.println("3. Verificare acum (cu restaurare automata)");
        System.out.println("4. Verificare acum (doar raport, fara restaurare)");
        System.out.println("5. Porneste monitorizarea periodica");
        System.out.println("6. Opreste monitorizarea");
        System.out.println("7. Afiseaza jurnalul");
        System.out.println("8. Windows: sfc /verifyonly (doar citire)");
        System.out.println("9. Windows: sfc /scannow (necesita administrator, cu confirmare)");
        System.out.println("10. Simuleaza deteriorare (doar in folderul demo)");
        System.out.println("0. Inapoi");
        System.out.print("Alegere: ");
    }

    public static void main(String[] args) {
        System.out.println("Folder de lucru: " + BAZA);
        try {
            initializare();
            ruleazaMeniu();
        } catch (Exception e) {
            System.out.println("Eroare fatala: " + e.getMessage());
        }
        System.out.println("La revedere!");
    }

    // bucla meniului sarcinii 19; se termina la optiunea 0
    static void ruleazaMeniu() {
        while (true) {
            meniu();
            if (!scanner.hasNextLine()) {
                opresteMonitorizarea();
                return;
            }
            String alegere = scanner.nextLine().trim();
            try {
                switch (alegere) {
                    case "1":
                        initializare();
                        break;
                    case "2":
                        backupManual(true);
                        break;
                    case "3": {
                        int n = verifica(true, true);
                        if (n == 0)
                            jurnal("OK", "Toate fisierele sunt intacte.");
                        break;
                    }
                    case "4": {
                        int n = verifica(false, true);
                        if (n == 0)
                            jurnal("OK", "Toate fisierele sunt intacte.");
                        break;
                    }
                    case "5": {
                        System.out.print("Interval in secunde (minim 5): ");
                        int sec = Integer.parseInt(scanner.nextLine().trim());
                        pornesteMonitorizarea(Math.max(5, sec));
                        break;
                    }
                    case "6":
                        opresteMonitorizarea();
                        break;
                    case "7":
                        afiseazaJurnal();
                        break;
                    case "8":
                        ruleazaSfc("/verifyonly");
                        break;
                    case "9":
                        if (confirma("sfc /scannow poate repara fisiere reale ale Windows. Continuati? (d/n): "))
                            ruleazaSfc("/scannow");
                        break;
                    case "10":
                        simuleazaDeteriorare();
                        break;
                    case "0":
                        opresteMonitorizarea();
                        return;
                    default:
                        System.out.println("Optiune necunoscuta.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Valoare numerica invalida.");
            } catch (Exception e) {
                jurnal("EROARE", e.getMessage());
            }
        }
    }
}
