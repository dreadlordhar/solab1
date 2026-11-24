import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Lab5 {

  private static final boolean IS_WINDOWS = System.getProperty("os.name")
    .toLowerCase()
    .contains("win");

  private static final Path TEMP_DIR = IS_WINDOWS
    ? Paths.get("C:\\Windows\\Temp")
    : Paths.get("/tmp");

  private static final Path LOG_FILE = Paths.get(
    System.getProperty("user.home"),
    "temp_cleaner_log.txt"
  );

  private static final String TASK_NAME = "Lab5";

  private static long minAgeHours = 24;

  public static void main(String[] args) throws Exception {
    String mode = "";
    for (String a : args) {
      if (a.startsWith("--")) {
        mode = a;
      } else {
        minAgeHours = Long.parseLong(a);
      }
    }

    switch (mode) {
      case "--run":
        cleanTemp();
        break;
      case "--schedule":
        scheduleDaily();
        break;
      case "--unschedule":
        unschedule();
        break;
      default:
        scheduleDaily();
        cleanTemp();
    }
  }

  private static void cleanTemp() throws IOException {
    log(
      "Pornire curatare: " +
        TEMP_DIR +
        " (fisiere mai vechi de " +
        minAgeHours +
        " h)"
    );

    final long limit = System.currentTimeMillis() - minAgeHours * 3600L * 1000L;
    final int[] deleted = { 0 };
    final int[] skipped = { 0 };
    final long[] bytes = { 0 };

    Files.walkFileTree(
      TEMP_DIR,
      new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          if (!attrs.isRegularFile()) {
            return FileVisitResult.CONTINUE;
          }
          if (attrs.lastModifiedTime().toMillis() > limit) {
            return FileVisitResult.CONTINUE;
          }
          try {
            long size = attrs.size();
            Files.delete(file);
            deleted[0]++;
            bytes[0] += size;
            System.out.println("Sters: " + file);
          } catch (IOException e) {
            skipped[0]++;
            System.out.println(
              "Omis:  " + file + " (" + e.getClass().getSimpleName() + ")"
            );
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
          skipped[0]++;
          return FileVisitResult.CONTINUE;
        }
      }
    );

    log(
      "Terminat: sterse " +
        deleted[0] +
        " fisiere, omise " +
        skipped[0] +
        ", eliberat " +
        bytes[0] / 1024 +
        " KB"
    );
  }

  private static String runCommandLine() throws Exception {
    String java = Paths.get(
      System.getProperty("java.home"),
      "bin",
      IS_WINDOWS ? "java.exe" : "java"
    ).toString();
    String classDir = Paths.get(
      Lab5.class
        .getProtectionDomain()
        .getCodeSource()
        .getLocation()
        .toURI()
    ).toString();
    if (classDir.endsWith(".java")) {
      throw new IllegalStateException(
        "Compileaza mai intai: javac Lab5.java, apoi java Lab5"
      );
    }
    return "\"" + java + "\" -cp \"" + classDir + "\" Lab5 --run";
  }

  private static Path batFile() {
    String base = System.getenv("ProgramData");
    if (base == null) {
      base = "C:\\ProgramData";
    }
    return Paths.get(base, TASK_NAME, "run.bat");
  }

  private static void scheduleDaily() throws Exception {
    String command = runCommandLine();
    int code;
    if (IS_WINDOWS) {
      Path bat = batFile();
      Files.createDirectories(bat.getParent());
      Files.write(
        bat,
        (
          "@echo off" +
          System.lineSeparator() +
          command +
          System.lineSeparator()
        ).getBytes()
      );
      code = exec(
        "schtasks",
        "/create",
        "/tn",
        TASK_NAME,
        "/tr",
        bat.toString(),
        "/sc",
        "DAILY",
        "/st",
        "03:00",
        "/f"
      );
    } else {
      String job = "0 3 * * * " + command + " # " + TASK_NAME;
      String shell =
        "(crontab -l 2>/dev/null | grep -v '# " +
        TASK_NAME +
        "'; echo '" +
        job +
        "') | crontab -";
      code = exec("sh", "-c", shell);
    }
    if (code == 0) {
      log("Sarcina zilnica '" + TASK_NAME + "' a fost programata (03:00)");
    } else {
      log("EROARE: sarcina nu a fost programata (cod " + code + ")");
    }
  }

  private static void unschedule() throws Exception {
    if (IS_WINDOWS) {
      exec("schtasks", "/delete", "/tn", TASK_NAME, "/f");
      Files.deleteIfExists(batFile());
    } else {
      exec(
        "sh",
        "-c",
        "crontab -l 2>/dev/null | grep -v '# " + TASK_NAME + "' | crontab -"
      );
    }
    log("Sarcina '" + TASK_NAME + "' a fost eliminata");
  }

  private static int exec(String... cmd)
    throws IOException, InterruptedException
  {
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.redirectErrorStream(true);
    Process p = pb.start();
    try (
      BufferedReader r = new BufferedReader(
        new InputStreamReader(p.getInputStream())
      )
    ) {
      String line;
      while ((line = r.readLine()) != null) {
        System.out.println(line);
      }
    }
    int code = p.waitFor();
    System.out.println("Cod de terminare: " + code);
    return code;
  }

  private static void log(String message) {
    String line =
      LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      ) +
      " - " +
      message;
    System.out.println(line);
    try {
      Files.write(
        LOG_FILE,
        (line + System.lineSeparator()).getBytes(),
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND
      );
    } catch (IOException e) {
      System.out.println("Nu s-a putut scrie in jurnal.");
    }
  }
}
