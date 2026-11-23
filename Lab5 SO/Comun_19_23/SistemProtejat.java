/*
 * Lucrarea de laborator 5, Sisteme de Operare
 * Program comun: sarcina 23 (Zaicico Axinia) depinde de sarcina 19 (colega).
 *
 *   Sarcina 19 - IntegritateSistem.java  (folosita fara nicio modificare)
 *       verifica periodic integritatea fisierelor din ~/SO_Lab5/monitorizat,
 *       le restaureaza din backup/referinta, face backup automat/manual, jurnal.log
 *   Sarcina 23 - FolderEncryptor.java    (folosita fara nicio modificare)
 *       urmareste un folder, cripteaza fisierele noi (AES-256-GCM),
 *       le muta intr-un director protejat si tine un jurnal al modificarilor
 *
 * Legatura dintre ele (dependenta 23 -> 19):
 *   1. Directorul protejat al sarcinii 23 ESTE folderul monitorizat al sarcinii 19
 *      (IntegritateSistem.MONITORIZAT), deci fisierele criptate sunt pazite de verificatorul de integritate:
 *      daca un .enc este sters sau modificat, sarcina 19 il restaureaza din backup.
 *   2. Dupa fiecare criptare, fisierul .enc si manifest.csv sunt copiate in backup-ul de referinta
 *      cu IntegritateSistem.copiaza(), iar copia se verifica cu IntegritateSistem.sha256().
 *   3. Criptarea se face sub IntegritateSistem.BLOCARE, ca verificarea periodica sa nu vada
 *      fisiere scrise pe jumatate si sa nu "restaureze" manifestul in timpul unei actualizari.
 *   4. Evenimentele importante ajung si in jurnalul comun (IntegritateSistem.jurnal -> jurnal.log).
 *   5. Inainte de decriptare se ruleaza IntegritateSistem.verifica(true, true), deci se decripteaza
 *      doar fisiere intacte sau abia restaurate.
 *   Pornirea foloseste IntegritateSistem.initializare(), pornesteMonitorizarea(), opresteMonitorizarea(),
 *   backupManual(), afiseazaJurnal() si scanner-ul ei pentru meniu.
 *
 * Compilare:  javac -encoding UTF-8 IntegritateSistem.java FolderEncryptor.java SistemProtejat.java
 * Pornire:    java SistemProtejat [--interval SECUNDE]            (meniu interactiv)
 *             java SistemProtejat --auto SECUNDE [--interval S]  (fara meniu, ruleaza SECUNDE secunde)
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SistemProtejat extends FolderEncryptor {

    // toate caile sunt construite pe baza folderului de lucru al sarcinii 19
    static final Path URMARIT = IntegritateSistem.BAZA.resolve("urmarit");
    static final Path PROTEJAT = IntegritateSistem.MONITORIZAT;
    static final Path CHEIE = IntegritateSistem.BAZA.resolve("cheie").resolve("aes256.key");
    static final Path JURNAL_CRIPTARE = IntegritateSistem.BAZA.resolve("jurnal_criptare.log");
    static final Path RESTAURAT = IntegritateSistem.BAZA.resolve("decriptat");

    SistemProtejat() throws IOException, GeneralSecurityException {
        super(URMARIT, PROTEJAT, CHEIE, JURNAL_CRIPTARE);
    }

    /**
     * Criptarea din sarcina 23, extinsa cu serviciile sarcinii 19:
     * blocare comuna, copiere in backup-ul de referinta, verificare SHA-256, jurnal comun.
     */
    @Override
    public Path encryptFile(Path src) throws IOException, GeneralSecurityException {
        synchronized (IntegritateSistem.BLOCARE) {
            Path enc = super.encryptFile(src);
            Path relEnc = PROTEJAT.relativize(enc);
            Path relManifest = PROTEJAT.relativize(getManifestFile());
            try {
                IntegritateSistem.copiaza(PROTEJAT, IntegritateSistem.REFERINTA, relEnc);
                IntegritateSistem.copiaza(PROTEJAT, IntegritateSistem.REFERINTA, relManifest);
                boolean ok = IntegritateSistem.sha256(enc)
                        .equals(IntegritateSistem.sha256(IntegritateSistem.REFERINTA.resolve(relEnc)));
                IntegritateSistem.jurnal("CRIPTARE", src.getFileName() + " criptat si mutat in "
                        + PROTEJAT.getFileName() + "/" + relEnc.toString().replace('\\', '/')
                        + "; adaugat in backup-ul de referinta"
                        + (ok ? " (SHA-256 verificat)" : " (SHA-256 DIFERIT!)"));
            } catch (Exception e) {
                IntegritateSistem.jurnal("EROARE", "Backup-ul pentru " + relEnc + " a esuat: " + e.getMessage());
            }
            return enc;
        }
    }

    /** Fisierele .enc din directorul protejat. */
    static List<Path> fisiereCriptate() throws IOException {
        try (Stream<Path> s = Files.walk(PROTEJAT)) {
            return s.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(ENC_EXT))
                    .sorted().collect(Collectors.toList());
        }
    }

    /** Verifica integritatea (sarcina 19, cu restaurare) si apoi decripteaza totul in ~/SO_Lab5/decriptat. */
    void decripteazaTot() throws Exception {
        int deteriorate = IntegritateSistem.verifica(true, true);
        if (deteriorate > 0) {
            IntegritateSistem.jurnal("INFO", deteriorate + " fisier(e) restaurate inainte de decriptare.");
        }
        int ok = 0;
        List<Path> lista = fisiereCriptate();
        for (Path f : lista) {
            try {
                Path out = decryptFile(f, RESTAURAT, getKey());
                System.out.println("Decriptat: " + PROTEJAT.relativize(f) + "  ->  " + out);
                ok++;
            } catch (GeneralSecurityException e) {
                IntegritateSistem.jurnal("EROARE", "Decriptare esuata: " + e.getMessage());
            }
        }
        IntegritateSistem.jurnal("DECRIPTARE", ok + " din " + lista.size() + " fisier(e) decriptate in " + RESTAURAT);
    }

    /** Demonstratie: sterge un fisier criptat si modifica un octet din altul, in directorul protejat. */
    void simuleazaAtac() throws Exception {
        synchronized (IntegritateSistem.BLOCARE) {
            List<Path> lista = fisiereCriptate();
            if (lista.isEmpty()) {
                System.out.println("Nu exista inca fisiere criptate. Copiati ceva in " + URMARIT);
                return;
            }
            Path sters = lista.get(0);
            Files.delete(sters);
            System.out.println("Simulat: sters " + PROTEJAT.relativize(sters));
            if (lista.size() > 1) {
                Path alterat = lista.get(1);
                byte[] b = Files.readAllBytes(alterat);
                b[b.length / 2] ^= 0x01;
                Files.write(alterat, b, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Simulat: modificat un octet in " + PROTEJAT.relativize(alterat));
            }
        }
    }

    static void meniu() {
        System.out.println("\n===== Sistem protejat (sarcina 23 + sarcina 19) =====");
        System.out.println("Copiati fisiere in: " + URMARIT);
        System.out.println("1. Verificare integritate acum (cu restaurare)");
        System.out.println("2. Backup manual al directorului protejat");
        System.out.println("3. Verificare + decriptare tuturor fisierelor");
        System.out.println("4. Simuleaza atac asupra fisierelor criptate");
        System.out.println("5. Afiseaza jurnalul comun");
        System.out.println("6. Lista fisierelor criptate");
        System.out.println("0. Iesire");
        System.out.print("Alegere: ");
    }

    public static void main(String[] args) throws Exception {
        int interval = 10;
        long auto = 0;
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--interval")) interval = Math.max(5, Integer.parseInt(args[i + 1]));
            if (args[i].equals("--auto")) auto = Long.parseLong(args[i + 1]);
        }

        System.out.println("Folder de lucru: " + IntegritateSistem.BAZA);
        SistemProtejat sp = new SistemProtejat();          // sarcina 23: cheie, director protejat, manifest
        IntegritateSistem.initializare();                   // sarcina 19: foldere + backup initial
        IntegritateSistem.pornesteMonitorizarea(interval);  // sarcina 19: verificare periodica

        Thread urmarire = new Thread(() -> {
            try {
                sp.watch(0);                                // sarcina 23: urmarire + criptare
            } catch (Exception e) {
                if (!(e instanceof java.nio.file.ClosedWatchServiceException)) {
                    IntegritateSistem.jurnal("EROARE", "Urmarirea folderului s-a oprit: " + e);
                }
            }
        }, "urmarire-folder");
        urmarire.setDaemon(true);
        urmarire.start();

        if (auto > 0) {
            Thread.sleep(auto * 1000);
        } else {
            meniuInteractiv(sp);
        }

        sp.stop();
        IntegritateSistem.opresteMonitorizarea();
        System.out.println("La revedere!");
    }

    private static void meniuInteractiv(SistemProtejat sp) {
        while (true) {
            meniu();
            if (!IntegritateSistem.scanner.hasNextLine()) return;
            String alegere = IntegritateSistem.scanner.nextLine().trim();
            try {
                switch (alegere) {
                    case "1": {
                        int n = IntegritateSistem.verifica(true, true);
                        if (n == 0) IntegritateSistem.jurnal("OK", "Toate fisierele sunt intacte.");
                        break;
                    }
                    case "2":
                        IntegritateSistem.backupManual(true);
                        break;
                    case "3":
                        sp.decripteazaTot();
                        break;
                    case "4":
                        sp.simuleazaAtac();
                        break;
                    case "5":
                        IntegritateSistem.afiseazaJurnal();
                        break;
                    case "6":
                        for (Path p : fisiereCriptate()) {
                            System.out.println("  " + PROTEJAT.relativize(p) + "  (" + Files.size(p) + " B)");
                        }
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Optiune necunoscuta.");
                }
            } catch (Exception e) {
                IntegritateSistem.jurnal("EROARE", e.toString());
            }
        }
    }
}
