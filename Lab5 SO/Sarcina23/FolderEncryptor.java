// sarcina 23: urmarirea unui folder si criptarea fisierelor noi (autor: zaicico axinia, gr. ia-242)

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FolderEncryptor {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TS_FILE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    // semnatura de la inceputul fiecarui fisier .enc
    private static final byte[] MAGIC = "SO5ENC01".getBytes(StandardCharsets.US_ASCII);
    // aes in modul gcm: cripteaza si detecteaza modificarile
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    // vector de initializare, nou pentru fiecare fisier
    private static final int IV_LEN = 12;
    // eticheta de autentificare gcm
    private static final int TAG_BITS = 128;
    // pauza ca fisierul sa fie scris complet inainte de criptare
    private static final long DEBOUNCE_MS = 1500;
    private static final int MAX_ATTEMPTS = 20;
    public static final String ENC_EXT = ".enc";

    private final Path watchDir;
    private final Path protectedDir;
    private final Path logFile;
    private final Path manifestFile;
    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();
    private final WatchService watcher;
    private final Map<WatchKey, Path> watchKeys = new HashMap<>();
    // criptari programate, cel mult una pentru fiecare fisier
    private final Map<Path, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
    // fisiere sterse chiar de program dupa criptare
    private final Set<Path> movedByUs = ConcurrentHashMap.newKeySet();
    // firul separat in care se face criptarea
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "criptare");
        t.setDaemon(true);
        return t;
    });
    private final AtomicInteger encrypted = new AtomicInteger();
    private final AtomicInteger errors = new AtomicInteger();
    private volatile boolean stopped = false;
    // se deblocheaza cand urmarirea a pornit (asteptat de main)
    private final CountDownLatch ready = new CountDownLatch(1);
    private final java.util.concurrent.atomic.AtomicBoolean stopping = new java.util.concurrent.atomic.AtomicBoolean();

    // verifica folderele, incarca cheia si protejeaza directoarele
    public FolderEncryptor(Path watchDir, Path protectedDir, Path keyFile, Path logFile)
            throws IOException, GeneralSecurityException {
        this.watchDir = watchDir.toAbsolutePath().normalize();
        this.protectedDir = protectedDir.toAbsolutePath().normalize();
        this.logFile = logFile.toAbsolutePath().normalize();
        this.manifestFile = this.protectedDir.resolve("manifest.csv");

        if (this.protectedDir.startsWith(this.watchDir) || this.watchDir.startsWith(this.protectedDir)) {
            throw new IllegalArgumentException("Directorul protejat nu poate fi in interiorul folderului urmarit (si invers).");
        }
        if (this.logFile.startsWith(this.watchDir)) {
            throw new IllegalArgumentException("Jurnalul nu poate fi in folderul urmarit (ar genera evenimente la fiecare scriere).");
        }

        Files.createDirectories(this.watchDir);
        Files.createDirectories(this.protectedDir);
        if (this.logFile.getParent() != null) Files.createDirectories(this.logFile.getParent());

        this.key = loadOrCreateKey(keyFile.toAbsolutePath().normalize());
        protectDirectory(this.protectedDir);
        protectDirectory(keyFile.toAbsolutePath().normalize().getParent());

        if (Files.notExists(manifestFile)) {
            Files.writeString(manifestFile,
                    "data;fisier_original;fisier_criptat;dimensiune_octeti;sha256_original;sha256_criptat\n",
                    StandardCharsets.UTF_8);
        }
        this.watcher = FileSystems.getDefault().newWatchService();
    }

    public Path getWatchDir() { return watchDir; }
    public Path getProtectedDir() { return protectedDir; }
    public Path getManifestFile() { return manifestFile; }
    public SecretKey getKey() { return key; }

    // scrie evenimentul pe ecran si in jurnal
    public synchronized void log(String event, String path, String details) {
        String line = String.format("%s | %-16s | %-40s | %s",
                LocalDateTime.now().format(TS), event, path, details == null ? "" : details);
        System.out.println(line);
        try {
            Files.writeString(logFile, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Nu pot scrie in jurnal: " + e.getMessage());
        }
    }

    private String rel(Path p) {
        Path abs = p.toAbsolutePath().normalize();
        return abs.startsWith(watchDir) ? watchDir.getFileName() + "/" + watchDir.relativize(abs).toString().replace('\\', '/')
                                        : abs.toString();
    }

    // citeste cheia aes-256 sau o genereaza la prima pornire
    private SecretKey loadOrCreateKey(Path keyFile) throws IOException, GeneralSecurityException {
        Files.createDirectories(keyFile.getParent());
        if (Files.exists(keyFile)) {
            byte[] raw = Base64.getDecoder().decode(Files.readString(keyFile).trim());
            if (raw.length != 32) throw new GeneralSecurityException("Cheia din " + keyFile + " nu are 256 de biti.");
            return new SecretKeySpec(raw, "AES");
        }
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, SecureRandom.getInstanceStrong());
        SecretKey k = kg.generateKey();
        Files.writeString(keyFile, Base64.getEncoder().encodeToString(k.getEncoded()), StandardCharsets.US_ASCII);
        System.out.println("Cheie AES-256 noua generata: " + keyFile);
        return k;
    }

    // acces doar pentru utilizatorul curent: icacls pe windows, chmod 700 pe linux
    private void protectDirectory(Path dir) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                String domain = System.getenv("USERDOMAIN");
                String user = (domain == null || domain.isEmpty() ? "" : domain + "\\")
                        + System.getProperty("user.name");
                int code = runCommand("icacls", dir.toString(), "/inheritance:r",
                        "/grant:r", user + ":(OI)(CI)F");
                log("PROTEJARE", dir.toString(), "icacls, acces doar pentru " + user + " (cod iesire " + code + ")");
            } else {
                Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("rwx------"));
                log("PROTEJARE", dir.toString(), "chmod 700");
            }
        } catch (Exception e) {
            log("AVERTISMENT", dir.toString(), "directorul nu a putut fi protejat: " + e.getMessage());
        }
    }

    // porneste o comanda de sistem si intoarce codul de iesire
    static int runCommand(String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            while (r.readLine() != null) {  }
        }
        return p.waitFor();
    }

    // antetul .enc: semnatura, numele fisierului si iv; este si el autentificat (aad)
    private static byte[] header(byte[] name, byte[] iv) {
        return ByteBuffer.allocate(MAGIC.length + 2 + name.length + iv.length)
                .put(MAGIC).putShort((short) name.length).put(name).put(iv).array();
    }

    // cripteaza fisierul, il pune in folderul protejat si sterge originalul
    public Path encryptFile(Path src) throws IOException, GeneralSecurityException {
        src = src.toAbsolutePath().normalize();
        Path relative = watchDir.relativize(src);
        Path target = uniqueName(protectedDir.resolve(relative.toString() + ENC_EXT));
        Files.createDirectories(target.getParent());
        Path tmp = target.resolveSibling(target.getFileName() + ".part");

        byte[] name = relative.toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8);
        byte[] iv = new byte[IV_LEN];
        random.nextBytes(iv);
        byte[] hdr = header(name, iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD(hdr);

        MessageDigest shaPlain = MessageDigest.getInstance("SHA-256");
        MessageDigest shaEnc = MessageDigest.getInstance("SHA-256");
        long size;
        try (InputStream in = new DigestInputStream(Files.newInputStream(src), shaPlain);
             OutputStream raw = new DigestOutputStream(Files.newOutputStream(tmp), shaEnc);
             CipherOutputStream out = new CipherOutputStream(raw, cipher)) {
            raw.write(hdr);
            size = in.transferTo(out);
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(tmp);
            throw e;
        }
        // se scrie intai in .part si apoi se redenumeste, ca sa nu ramana fisiere pe jumatate
        Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
        HexFormat hex = HexFormat.of();
        String plainHash = hex.formatHex(shaPlain.digest());
        String encHash = hex.formatHex(shaEnc.digest());
        log("CRIPTARE", rel(src), size + " B, AES-256-GCM, SHA-256 " + plainHash.substring(0, 16) + "...");

        // originalul se sterge abia dupa ce copia criptata este gata
        movedByUs.add(src);
        try {
            Files.delete(src);
        } catch (IOException e) {
            movedByUs.remove(src);
            Files.deleteIfExists(target);
            throw e;
        }

        synchronized (this) {
            Files.writeString(manifestFile, String.join(";",
                            LocalDateTime.now().format(TS), relative.toString().replace('\\', '/'),
                            protectedDir.relativize(target).toString().replace('\\', '/'),
                            String.valueOf(size), plainHash, encHash) + "\n",
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
        log("MUTARE", rel(src), "in " + protectedDir.getFileName() + "/" + protectedDir.relativize(target).toString().replace('\\', '/'));
        encrypted.incrementAndGet();
        return target;
    }

    private static Path uniqueName(Path p) {
        if (Files.notExists(p)) return p;
        String n = p.getFileName().toString();
        String base = n.substring(0, n.length() - ENC_EXT.length());
        return p.resolveSibling(base + "_" + LocalDateTime.now().format(TS_FILE) + ENC_EXT);
    }

    // decripteaza un .enc si verifica eticheta gcm
    public static Path decryptFile(Path enc, Path outDir, SecretKey key) throws IOException, GeneralSecurityException {
        outDir = outDir.toAbsolutePath().normalize();
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(enc)))) {
            byte[] magic = in.readNBytes(MAGIC.length);
            if (!Arrays.equals(magic, MAGIC)) throw new GeneralSecurityException("Nu este un fisier criptat de acest program: " + enc);
            byte[] name = in.readNBytes(in.readUnsignedShort());
            byte[] iv = in.readNBytes(IV_LEN);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(header(name, iv));

            Path out = outDir.resolve(new String(name, StandardCharsets.UTF_8)).normalize();
            if (!out.startsWith(outDir)) throw new GeneralSecurityException("Nume de fisier invalid in antet: " + enc);
            Files.createDirectories(out.getParent());
            Path tmp = out.resolveSibling(out.getFileName() + ".part");
            try (CipherInputStream cin = new CipherInputStream(in, cipher);
                 OutputStream os = Files.newOutputStream(tmp)) {
                cin.transferTo(os);
            } catch (IOException e) {
                Files.deleteIfExists(tmp);
                throw new GeneralSecurityException("Fisierul " + enc + " a fost modificat sau cheia este gresita.", e);
            }
            Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING);
            return out;
        }
    }

    // watchservice anunta cand un fisier apare, se schimba sau se sterge
    private void register(Path dir) throws IOException {
        WatchKey k = dir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
        watchKeys.put(k, dir);
    }

    private void registerAll(Path start) throws IOException {
        try (Stream<Path> s = Files.walk(start)) {
            for (Path d : (Iterable<Path>) s.filter(Files::isDirectory)::iterator) register(d);
        }
    }

    // fisierele temporare ale editoarelor nu se cripteaza
    private static boolean isIgnored(Path p) {
        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
        return n.startsWith("~$") || n.startsWith(".~") || n.endsWith(".tmp") || n.endsWith(".part")
                || n.endsWith(".crdownload") || n.endsWith(".partial") || n.endsWith(".swp") || n.endsWith("~");
    }

    // programeaza criptarea dupa o pauza; un eveniment nou reporneste pauza
    private void schedule(Path file, long delayMs, int attempt) {
        ScheduledFuture<?> f;
        try {
            f = worker.schedule(() -> process(file, attempt), delayMs, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.RejectedExecutionException e) {
            return;
        }
        ScheduledFuture<?> old = pending.put(file, f);
        if (old != null && old != f) old.cancel(false);
    }

    // verifica ca fisierul nu se mai scrie, apoi il cripteaza (cu reincercari)
    private void process(Path file, int attempt) {
        pending.remove(file);
        if (!Files.isRegularFile(file)) return;
        try {
            long s1 = Files.size(file);
            Thread.sleep(300);
            if (Files.size(file) != s1) {
                schedule(file, DEBOUNCE_MS, attempt);
                return;
            }
            encryptFile(file);
        } catch (Exception e) {
            if (attempt < MAX_ATTEMPTS && Files.exists(file)) {
                log("REINCERCARE", rel(file), "fisier ocupat (" + e.getClass().getSimpleName() + "), incercarea " + (attempt + 1));
                schedule(file, DEBOUNCE_MS, attempt + 1);
            } else {
                errors.incrementAndGet();
                log("EROARE", rel(file), e.toString());
            }
        }
    }

    private void handleNewFile(Path p) {
        if (isIgnored(p)) {
            log("IGNORAT", rel(p), "fisier temporar, nu se cripteaza");
        } else {
            schedule(p, DEBOUNCE_MS, 0);
        }
    }

    // bucla principala: asculta evenimentele folderului urmarit
    public void watch(long durataSec) throws IOException, InterruptedException {
        log("START", rel(watchDir), "director protejat: " + protectedDir);
        registerAll(watchDir);

        try (Stream<Path> s = Files.walk(watchDir)) {
            s.filter(Files::isRegularFile).forEach(p -> {
                log("EXISTENT", rel(p), "gasit la pornire");
                handleNewFile(p);
            });
        }

        ready.countDown();

        long deadline = durataSec > 0 ? System.currentTimeMillis() + durataSec * 1000 : Long.MAX_VALUE;
        while (!stopped) {
            long left = deadline - System.currentTimeMillis();
            if (left <= 0) break;
            WatchKey wk;
            try {
                wk = watcher.poll(Math.min(left, 1000), TimeUnit.MILLISECONDS);
            } catch (ClosedWatchServiceException e) {
                break;
            }
            if (wk == null) continue;
            Path dir = watchKeys.get(wk);
            if (dir == null) { wk.reset(); continue; }

            for (WatchEvent<?> ev : wk.pollEvents()) {
                WatchEvent.Kind<?> kind = ev.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    log("OVERFLOW", rel(dir), "s-au pierdut evenimente, se rescaneaza folderul");
                    try (Stream<Path> s = Files.walk(dir)) {
                        s.filter(Files::isRegularFile).forEach(this::handleNewFile);
                    }
                    continue;
                }
                Path child = dir.resolve((Path) ev.context());

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (Files.isDirectory(child)) {
                        log("CREARE_DIR", rel(child), "subfolder nou, este urmarit si el");
                        registerAll(child);
                        try (Stream<Path> s = Files.walk(child)) {
                            s.filter(Files::isRegularFile).forEach(p -> {
                                log("CREARE", rel(p), "in subfolder nou");
                                handleNewFile(p);
                            });
                        }
                    } else {
                        log("CREARE", rel(child), sizeOf(child));
                        handleNewFile(child);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (Files.isRegularFile(child)) {
                        log("MODIFICARE", rel(child), sizeOf(child));
                        if (!isIgnored(child)) schedule(child, DEBOUNCE_MS, 0);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    if (movedByUs.remove(child)) {
                        log("STERGERE", rel(child), "originalul a fost eliminat dupa criptare");
                    } else {
                        log("STERGERE", rel(child), "sters / redenumit de utilizator");
                    }
                }
            }
            if (!wk.reset()) {
                watchKeys.remove(wk);
                if (dir.equals(watchDir)) {
                    log("EROARE", rel(watchDir), "folderul urmarit nu mai este accesibil");
                    break;
                }
            }
        }
        stop();
    }

    private static String sizeOf(Path p) {
        try { return Files.size(p) + " B"; } catch (IOException e) { return ""; }
    }

    // opreste urmarirea si asteapta criptarile in curs
    public void stop() {
        if (!stopping.compareAndSet(false, true)) return;
        stopped = true;
        ready.countDown();
        worker.shutdown();
        try { worker.awaitTermination(10, TimeUnit.SECONDS); } catch (InterruptedException ignored) { }
        try { watcher.close(); } catch (IOException ignored) { }
        log("STOP", rel(watchDir), "fisiere criptate: " + encrypted.get() + ", erori: " + errors.get());
    }

    public boolean awaitReady(long millis) throws InterruptedException {
        return ready.await(millis, TimeUnit.MILLISECONDS);
    }

    public int getEncryptedCount() { return encrypted.get(); }

    // pornire singura: urmarit, protejat, --durata sau decrypt
    public static void main(String[] args) throws Exception {
        List<String> a = new ArrayList<>(Arrays.asList(args));
        long durata = 0;
        int i = a.indexOf("--durata");
        if (i >= 0) {
            durata = Long.parseLong(a.get(i + 1));
            a.remove(i + 1);
            a.remove(i);
        }
        Path keyFile = Path.of("cheie", "aes256.key");

        if (!a.isEmpty() && a.get(0).equalsIgnoreCase("decrypt")) {
            if (a.size() < 3) {
                System.out.println("Utilizare: java FolderEncryptor decrypt <fisier.enc | folderProtejat> <folderIesire>");
                return;
            }
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(Files.readString(keyFile).trim()), "AES");
            Path src = Path.of(a.get(1));
            Path out = Path.of(a.get(2));
            List<Path> files = new ArrayList<>();
            if (Files.isDirectory(src)) {
                try (Stream<Path> s = Files.walk(src)) {
                    s.filter(p -> p.toString().endsWith(ENC_EXT)).forEach(files::add);
                }
            } else {
                files.add(src);
            }
            int ok = 0;
            for (Path f : files) {
                try {
                    System.out.println("Decriptat: " + f.getFileName() + "  ->  " + decryptFile(f, out, key));
                    ok++;
                } catch (GeneralSecurityException e) {
                    System.out.println("ESEC: " + e.getMessage());
                }
            }
            System.out.println("Total decriptate: " + ok + " din " + files.size());
            return;
        }

        if (!a.isEmpty() && a.get(0).equalsIgnoreCase("watch")) a.remove(0);
        Path watch = Path.of(a.size() > 0 ? a.get(0) : "urmarit");
        Path prot = Path.of(a.size() > 1 ? a.get(1) : "protejat");

        FolderEncryptor fe = new FolderEncryptor(watch, prot, keyFile, Path.of("jurnal_sarcina23.log"));
        Runtime.getRuntime().addShutdownHook(new Thread(fe::stop));
        System.out.println("Se urmareste: " + fe.getWatchDir()
                + (durata > 0 ? "  (timp de " + durata + " s)" : "  (Ctrl+C pentru oprire)"));
        fe.watch(durata);
    }
}
