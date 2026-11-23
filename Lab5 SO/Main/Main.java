// meniu unic: comenzile sarcinii 19 (integritate, backup) si ale sarcinii 23 (criptare) lucreaza impreuna

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    static int interval = 10;

    // aceeasi consola ca in sarcina 19
    static final Scanner scanner = IntegritateSistem.scanner;

    // null cand urmarirea folderului este oprita
    static Protejat urmarire;

    // cai derivate din folderul de lucru al sarcinii 19
    static Path urmarit() { return IntegritateSistem.BAZA.resolve("urmarit"); }
    // dependenta principala: directorul protejat (23) este folderul monitorizat (19)
    static Path protejat() { return IntegritateSistem.MONITORIZAT; }
    static Path fisierCheie() { return IntegritateSistem.BAZA.resolve("cheie").resolve("aes256.key"); }
    static Path decriptat() { return IntegritateSistem.BAZA.resolve("decriptat"); }

    // returneaza null cand nu mai exista intrare
    static String citeste() {
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }

    static boolean monitorizarePornita() {
        return IntegritateSistem.planificator != null && !IntegritateSistem.planificator.isShutdown();
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--interval")) interval = Math.max(5, Integer.parseInt(args[i + 1]));
        }
        IntegritateSistem.folosesteBaza(Paths.get(System.getProperty("user.home"), "SO_Lab5_Main"));
        // verificarile reusite se scriu doar in jurnal, ca sa nu intrerupa meniul
        IntegritateSistem.OK_PERIODIC_IN_CONSOLA = false;
        System.out.println("Folder de lucru: " + IntegritateSistem.BAZA);

        // ordinea pasilor urmeaza dependentele: intai datele (23), apoi paza lor (19)
        System.out.println("\n[1/4] Sarcina 23: cheia AES-256 si directorul protejat");
        urmarire = new Protejat();
        System.out.println("[2/4] Sarcina 19: backup initial (referinta)");
        IntegritateSistem.initializare(false);
        System.out.println("[3/4] Sarcina 23: urmarirea folderului " + urmarit().getFileName());
        porneste(urmarire);
        System.out.println("[4/4] Sarcina 19: monitorizare la fiecare " + interval + " s");
        IntegritateSistem.pornesteMonitorizarea(interval, interval);

        try {
            while (true) {
                meniu();
                String a = citeste();
                if (a == null || a.equals("0")) break;
                try {
                    comanda(a);
                } catch (Exception e) {
                    IntegritateSistem.jurnal("EROARE", e.toString());
                }
            }
        } finally {
            if (urmarire != null) urmarire.stop();
            if (monitorizarePornita()) IntegritateSistem.opresteMonitorizarea();
        }
        System.out.println("La revedere!");
    }

    // (19) = sarcina 19, (23) = sarcina 23, (19+23) = folosesc amandoua
    static void meniu() {
        System.out.println("\n===== Sistem protejat (sarcina 19 + sarcina 23) =====");
        System.out.println("Copiati fisiere in: " + urmarit() + "  -> se cripteaza singure");
        System.out.println("Urmarire folder: " + (urmarire != null ? "PORNITA" : "oprita")
                + " | Monitorizare periodica: " + (monitorizarePornita() ? "PORNITA (" + interval + " s)" : "oprita"));
        System.out.println("1. Verificare integritate acum (cu restaurare)            (19)");
        System.out.println("2. Backup manual al directorului protejat                  (19)");
        System.out.println("3. Verificare + decriptare tuturor fisierelor              (19+23)");
        System.out.println("4. Simuleaza atac asupra fisierelor criptate               (23+19)");
        System.out.println("5. Afiseaza jurnalul comun                                 (19+23)");
        System.out.println("6. Lista fisierelor criptate                               (23)");
        System.out.println("7. Sterge un fisier criptat (test)                         (23+19)");
        System.out.println("8. Modifica un fisier criptat (test)                       (23+19)");
        System.out.println("9. Verificare integritate, doar raport                     (19)");
        System.out.println("10. Porneste / opreste urmarirea folderului                (23)");
        System.out.println("11. Porneste / opreste monitorizarea periodica             (19)");
        System.out.println("12. Lista copiilor de siguranta (referinta si arhiva)      (19)");
        System.out.println("13. Fisiere deteriorate din jurnal (cu data detectarii)    (19)");
        System.out.println("14. Windows: sfc /verifyonly                               (19)");
        System.out.println("15. Windows: sfc /scannow (cu confirmare)                  (19)");
        System.out.println("16. Stare dependente: criptat (23) -> pazit si salvat (19) (19+23)");
        System.out.println("0. Iesire");
        System.out.print("Alegere: ");
    }

    static void comanda(String a) throws Exception {
        switch (a) {
            case "1": {
                int n = IntegritateSistem.verifica(true, true);
                if (n == 0) IntegritateSistem.jurnal("OK", "Toate fisierele sunt intacte.");
                break;
            }
            case "2": IntegritateSistem.backupManual(true); break;
            case "3": decripteazaTot(); break;
            case "4": simuleazaAtac(); break;
            case "5": IntegritateSistem.afiseazaJurnal(); break;
            case "6": listeaza(); break;
            case "7": stergeFisier(); break;
            case "8": modificaFisier(); break;
            case "9": {
                int n = IntegritateSistem.verifica(false, true);
                if (n == 0) IntegritateSistem.jurnal("OK", "Toate fisierele sunt intacte.");
                break;
            }
            case "10": comutaUrmarirea(); break;
            case "11": comutaMonitorizarea(); break;
            case "12": listaCopii(); break;
            case "13": fisiereDeteriorate(); break;
            case "14": IntegritateSistem.ruleazaSfc("/verifyonly"); break;
            case "15":
                if (IntegritateSistem.confirma("sfc /scannow poate repara fisiere reale ale Windows. Continuati? (d/n): "))
                    IntegritateSistem.ruleazaSfc("/scannow");
                break;
            case "16": stareDependente(); break;
            default: System.out.println("Optiune necunoscuta.");
        }
    }

    // criptorul sarcinii 23, legat de serviciile sarcinii 19
    static class Protejat extends FolderEncryptor {

        Protejat() throws IOException, GeneralSecurityException {
            super(urmarit(), protejat(), fisierCheie(), IntegritateSistem.JURNAL);
        }

        // evenimentele sarcinii 23 merg in jurnalul sarcinii 19 (un singur jurnal)
        @Override
        public synchronized void log(String event, String path, String details) {
            String detalii = (details == null || details.isEmpty()) ? "" : " - " + details;
            IntegritateSistem.jurnal(event, path + detalii);
        }

        // dupa criptare (23), fisierul .enc si manifestul intra in backup (19), cu verificare sha-256
        @Override
        public Path encryptFile(Path src) throws IOException, GeneralSecurityException {
            // lacatul comun: verificarea periodica nu vede fisiere scrise pe jumatate
            synchronized (IntegritateSistem.BLOCARE) {
                Path enc = super.encryptFile(src);
                Path relEnc = getProtectedDir().relativize(enc);
                Path relManifest = getProtectedDir().relativize(getManifestFile());
                try {
                    IntegritateSistem.copiaza(getProtectedDir(), IntegritateSistem.REFERINTA, relEnc);
                    IntegritateSistem.copiaza(getProtectedDir(), IntegritateSistem.REFERINTA, relManifest);
                    boolean ok = IntegritateSistem.sha256(enc)
                            .equals(IntegritateSistem.sha256(IntegritateSistem.REFERINTA.resolve(relEnc)));
                    IntegritateSistem.jurnal(ok ? "BACKUP" : "EROARE", relEnc.toString().replace('\\', '/')
                            + " adaugat in backup-ul de referinta" + (ok ? " (SHA-256 verificat)" : " (SHA-256 DIFERIT!)"));
                } catch (Exception e) {
                    IntegritateSistem.jurnal("EROARE", "Backup-ul pentru " + relEnc + " a esuat: " + e.getMessage());
                }
                return enc;
            }
        }
    }

    // ---------- comenzi care combina cele doua sarcini ----------

    // 3: intai verificarea cu restaurare (19), apoi decriptarea (23)
    static void decripteazaTot() throws Exception {
        int deteriorate = IntegritateSistem.verifica(true, true);
        if (deteriorate > 0) {
            IntegritateSistem.jurnal("INFO", deteriorate + " fisier(e) restaurate inainte de decriptare.");
        }
        // cheia se citeste din fisier, ca decriptarea sa mearga si cu urmarirea oprita
        SecretKey cheie = new SecretKeySpec(Base64.getDecoder().decode(Files.readString(fisierCheie()).trim()), "AES");
        List<Path> lista = fisiereEnc();
        int ok = 0;
        for (Path f : lista) {
            try {
                Path out = FolderEncryptor.decryptFile(f, decriptat(), cheie);
                System.out.println("Decriptat: " + protejat().relativize(f) + "  ->  " + out);
                ok++;
            } catch (GeneralSecurityException e) {
                IntegritateSistem.jurnal("EROARE", "Decriptare esuata: " + e.getMessage());
            }
        }
        IntegritateSistem.jurnal("DECRIPTARE", ok + " din " + lista.size() + " fisier(e) decriptate in " + decriptat());
    }

    // 4: sterge un fisier criptat si schimba un octet in altul; sarcina 19 le restaureaza
    static void simuleazaAtac() throws Exception {
        synchronized (IntegritateSistem.BLOCARE) {
            List<Path> lista = fisiereEnc();
            if (lista.isEmpty()) {
                System.out.println("Nu exista fisiere criptate. Copiati ceva in " + urmarit());
                return;
            }
            Files.delete(lista.get(0));
            System.out.println("Simulat: sters " + protejat().relativize(lista.get(0)));
            if (lista.size() > 1) {
                schimbaOctet(lista.get(1));
                System.out.println("Simulat: modificat un octet in " + protejat().relativize(lista.get(1)));
            }
        }
        mesajRestaurare();
    }

    // 7: sterge un fisier criptat ales de utilizator
    static void stergeFisier() throws Exception {
        Path f = alegeFisier();
        if (f == null) return;
        synchronized (IntegritateSistem.BLOCARE) {
            Files.delete(f);
        }
        System.out.println("Sters: " + protejat().relativize(f));
        mesajRestaurare();
    }

    // 8: modifica un octet dintr-un fisier criptat ales de utilizator
    static void modificaFisier() throws Exception {
        Path f = alegeFisier();
        if (f == null) return;
        synchronized (IntegritateSistem.BLOCARE) {
            schimbaOctet(f);
        }
        System.out.println("Modificat un octet in: " + protejat().relativize(f));
        mesajRestaurare();
    }

    static void mesajRestaurare() {
        if (monitorizarePornita()) {
            System.out.println("Sarcina 19 il restaureaza la urmatoarea verificare (sau alegeti optiunea 1).");
        } else {
            System.out.println("Monitorizarea este oprita: fisierul ramane stricat pana la optiunea 1 sau 11.");
        }
    }

    // 10: porneste sau opreste sarcina 23; la pornire se cripteaza si fisierele deja existente
    static void comutaUrmarirea() throws Exception {
        if (urmarire == null) {
            urmarire = new Protejat();
            porneste(urmarire);
        } else {
            urmarire.stop();
            urmarire = null;
            System.out.println("Fisierele noi din " + urmarit().getFileName() + " nu se mai cripteaza.");
        }
    }

    // 11: porneste sau opreste sarcina 19
    static void comutaMonitorizarea() {
        if (monitorizarePornita()) {
            IntegritateSistem.opresteMonitorizarea();
            System.out.println("Atentie: fisierele criptate nu mai sunt pazite pana la o verificare manuala (optiunea 1).");
        } else {
            IntegritateSistem.pornesteMonitorizarea(interval);
        }
    }

    // 16: arata legatura dintre cele doua sarcini pe datele reale
    static void stareDependente() throws Exception {
        synchronized (IntegritateSistem.BLOCARE) {
            System.out.println("\nSarcina 23 scrie fisierele criptate in: " + protejat());
            System.out.println("Sarcina 19 pazeste acelasi folder si il salveaza in: " + IntegritateSistem.REFERINTA);
            System.out.println("Urmarire folder (23): " + (urmarire != null ? "pornita" : "oprita")
                    + " | monitorizare periodica (19): " + (monitorizarePornita() ? "pornita" : "oprita"));
            List<Path> fisiere = IntegritateSistem.listaFisiere(protejat());
            if (fisiere.isEmpty()) {
                System.out.println("Nu exista inca fisiere in directorul protejat.");
                return;
            }
            System.out.printf("%-34s %-10s %s%n", "Fisier din directorul protejat", "In backup", "SHA-256 identic");
            for (Path rel : fisiere) {
                Path copie = IntegritateSistem.REFERINTA.resolve(rel);
                String backup = Files.exists(copie) ? "da" : "NU";
                String identic = Files.exists(copie)
                        ? (IntegritateSistem.sha256(protejat().resolve(rel)).equals(IntegritateSistem.sha256(copie)) ? "da" : "NU")
                        : "-";
                System.out.printf("%-34s %-10s %s%n", rel, backup, identic);
            }
        }
    }

    // ---------- comenzi ale sarcinii 19 ----------

    // 12: copia de referinta si copiile automate cu data in nume
    static void listaCopii() throws Exception {
        List<Path> ref = IntegritateSistem.listaFisiere(IntegritateSistem.REFERINTA);
        System.out.println("Referinta (folosita la restaurare): " + ref.size() + " fisier(e)");
        for (Path p : ref) System.out.println("  " + p);
        System.out.println("Arhiva (backup manual si automat):");
        if (Files.notExists(IntegritateSistem.ARHIVA)) {
            System.out.println("  (goala)");
            return;
        }
        try (Stream<Path> s = Files.list(IntegritateSistem.ARHIVA)) {
            List<Path> foldere = s.filter(Files::isDirectory).sorted().collect(Collectors.toList());
            if (foldere.isEmpty()) System.out.println("  (goala)");
            for (Path d : foldere) {
                System.out.println("  " + d.getFileName() + "  (" + IntegritateSistem.listaFisiere(d).size() + " fisier(e))");
            }
        }
    }

    // 13: doar liniile [DETERIORAT] din jurnal, fiecare cu data detectarii
    static void fisiereDeteriorate() throws Exception {
        if (Files.notExists(IntegritateSistem.JURNAL)) {
            System.out.println("Jurnalul este gol.");
            return;
        }
        List<String> linii = Files.readAllLines(IntegritateSistem.JURNAL).stream()
                .filter(l -> l.contains("[DETERIORAT]")).collect(Collectors.toList());
        linii.forEach(System.out::println);
        System.out.println("Total fisiere deteriorate detectate: " + linii.size());
    }

    // ---------- utilitare ----------

    // 6
    static void listeaza() throws IOException {
        List<Path> lista = fisiereEnc();
        if (lista.isEmpty()) System.out.println("Nu exista fisiere criptate.");
        for (Path p : lista) System.out.println("  " + protejat().relativize(p) + "  (" + Files.size(p) + " B)");
    }

    static List<Path> fisiereEnc() throws IOException {
        if (Files.notExists(protejat())) return List.of();
        try (Stream<Path> s = Files.walk(protejat())) {
            return s.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(FolderEncryptor.ENC_EXT))
                    .sorted().collect(Collectors.toList());
        }
    }

    // arata fisierele criptate numerotate si intoarce cel ales (null daca se renunta)
    static Path alegeFisier() throws IOException {
        List<Path> lista = fisiereEnc();
        if (lista.isEmpty()) {
            System.out.println("Nu exista fisiere criptate. Copiati ceva in " + urmarit());
            return null;
        }
        for (int i = 0; i < lista.size(); i++) System.out.println("  " + (i + 1) + ". " + protejat().relativize(lista.get(i)));
        System.out.print("Numarul fisierului (0 = renunt): ");
        String r = citeste();
        try {
            int n = Integer.parseInt(r == null ? "0" : r);
            return (n >= 1 && n <= lista.size()) ? lista.get(n - 1) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // schimba un singur bit; la decriptare, eticheta gcm detecteaza modificarea
    static void schimbaOctet(Path f) throws IOException {
        byte[] b = Files.readAllBytes(f);
        b[b.length / 2] ^= 0x01;
        Files.write(f, b, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // urmarirea ruleaza intr-un fir in fundal; asteptam sa fie gata inainte de meniu
    static void porneste(FolderEncryptor fe) throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                fe.watch(0);
            } catch (Exception e) {
                if (!(e instanceof ClosedWatchServiceException)) IntegritateSistem.jurnal("EROARE", "Urmarirea s-a oprit: " + e);
            }
        }, "urmarire-folder");
        t.setDaemon(true);
        t.start();
        if (!fe.awaitReady(5000)) System.out.println("Atentie: urmarirea nu a pornit la timp.");
    }
}
