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
import java.util.ArrayList;
import java.util.List;

public class Lab5 {

  private static final boolean IS_WINDOWS = System.getProperty("os.name")
    .toLowerCase()
    .contains("win");

  private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern(
    "yyyy-MM-dd HH:mm:ss"
  );

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      printUsage();
      return;
    }

    String app = args[0];
    String[] rest = new String[args.length - 1];
    System.arraycopy(args, 1, rest, 0, rest.length);

    switch (app) {
      case "temp":
        TempCleaner.run(rest);
        break;
      case "net":
        NetworkMonitor.run(rest);
        break;
      default:
        printUsage();
    }
  }

  private static void printUsage() {
    System.out.println("Utilizare:");
    System.out.println("  java Lab5 temp [--run|--schedule|--unschedule] [ore]");
    System.out.println("  java Lab5 net  [--run|--install|--uninstall] [--host H] [--iface I]");
  }

  private static int lastExit;

  private static String exec(String... cmd)
    throws IOException, InterruptedException
  {
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.redirectErrorStream(true);
    Process p = pb.start();
    StringBuilder out = new StringBuilder();
    try (
      BufferedReader r = new BufferedReader(
        new InputStreamReader(p.getInputStream())
      )
    ) {
      String line;
      while ((line = r.readLine()) != null) {
        out.append(line).append('\n');
      }
    }
    lastExit = p.waitFor();
    return out.toString();
  }

  private static void log(Path file, Path appDir, String message) {
    String line = LocalDateTime.now().format(TS) + " - " + message;
    System.out.println(line);
    try {
      if (appDir != null) Files.createDirectories(appDir);
      Files.write(
        file,
        (line + System.lineSeparator()).getBytes(),
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND
      );
    } catch (IOException e) {
      System.out.println("Nu s-a putut scrie in jurnal: " + e.getMessage());
    }
  }

  private static String runCommandLine(String mainApp, String extraArgs)
    throws Exception
  {
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
    return (
      "\"" + java + "\" -cp \"" + classDir + "\" Lab5 " + mainApp + " " +
      extraArgs
    ).trim();
  }

  static final class TempCleaner {

    private static final Path TEMP_DIR = IS_WINDOWS
      ? Paths.get("C:\\Windows\\Temp")
      : Paths.get("/tmp");

    private static final Path LOG_FILE = Paths.get(
      System.getProperty("user.home"),
      "temp_cleaner_log.txt"
    );

    private static final String TASK_NAME = "TempCleaner";
    private static long minAgeHours = 24;

    static void run(String[] args) throws Exception {
      String mode = "";
      for (String a : args) {
        if (a.startsWith("--")) mode = a;
        else minAgeHours = Long.parseLong(a);
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

      final long limit =
        System.currentTimeMillis() - minAgeHours * 3600L * 1000L;
      final int[] deleted = { 0 };
      final int[] skipped = { 0 };
      final long[] bytes = { 0 };

      Files.walkFileTree(
        TEMP_DIR,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(
            Path file,
            BasicFileAttributes attrs
          ) {
            if (!attrs.isRegularFile()) return FileVisitResult.CONTINUE;
            if (attrs.lastModifiedTime().toMillis() > limit)
              return FileVisitResult.CONTINUE;
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

    private static Path batFile() {
      String base = System.getenv("ProgramData");
      if (base == null) base = "C:\\ProgramData";
      return Paths.get(base, TASK_NAME, "run.bat");
    }

    private static void scheduleDaily() throws Exception {
      String command = runCommandLine("temp", "--run");
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
        exec(
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
        code = lastExit;
      } else {
        String job = "0 3 * * * " + command + " # " + TASK_NAME;
        String shell =
          "(crontab -l 2>/dev/null | grep -v '# " +
          TASK_NAME +
          "'; echo '" +
          job +
          "') | crontab -";
        exec("sh", "-c", shell);
        code = lastExit;
      }
      if (code == 0) log(
        "Sarcina zilnica '" + TASK_NAME + "' a fost programata (03:00)"
      );
      else log("EROARE: sarcina nu a fost programata (cod " + code + ")");
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

    private static void log(String message) {
      Lab5.log(LOG_FILE, null, message);
    }
  }

  static final class NetworkMonitor {

    private static final String TASK_NAME = "NetworkMonitor";
    private static final int INTERVAL_SECONDS = 10;
    private static final int FAIL_LIMIT = 3;
    private static final String DEFAULT_HOST = "8.8.8.8";

    private static String host = DEFAULT_HOST;
    private static String iface = null;

    private static final Path APP_DIR = IS_WINDOWS
      ? Paths.get(
          System.getenv("ProgramData") != null
            ? System.getenv("ProgramData")
            : "C:\\ProgramData",
          TASK_NAME
        )
      : Paths.get(System.getProperty("user.home"));

    private static final Path LOG_FILE = APP_DIR.resolve(
      "network_monitor_log.txt"
    );

    static void run(String[] args) throws Exception {
      String mode = "";
      for (int i = 0; i < args.length; i++) {
        switch (args[i]) {
          case "--host":
            host = args[++i];
            break;
          case "--iface":
            iface = args[++i];
            break;
          default:
            mode = args[i];
        }
      }

      switch (mode) {
        case "--run":
          monitor();
          break;
        case "--install":
          install();
          break;
        case "--uninstall":
          uninstall();
          break;
        default:
          install();
          monitor();
      }
    }

    private static void monitor() throws Exception {
      log(
        "Monitorizare pornita: ping " +
          host +
          " la fiecare " +
          INTERVAL_SECONDS +
          " s, repornire dupa " +
          FAIL_LIMIT +
          " esecuri consecutive"
      );

      int fails = 0;
      long downSince = 0;

      while (true) {
        if (ping()) {
          if (downSince != 0) {
            log(
              "Conexiune restabilita dupa " +
                (System.currentTimeMillis() - downSince) / 1000 +
                " s"
            );
            downSince = 0;
          }
          fails = 0;
        } else {
          fails++;
          if (downSince == 0) downSince = System.currentTimeMillis();
          log(
            "DEFECTIUNE: ping " +
              host +
              " esuat (" +
              fails +
              "/" +
              FAIL_LIMIT +
              ")"
          );
          if (fails >= FAIL_LIMIT) {
            log("Conexiune pierduta - se reporneste interfata de retea");
            restartInterface();
            fails = 0;
          }
        }
        Thread.sleep(INTERVAL_SECONDS * 1000L);
      }
    }

    private static boolean ping() throws IOException, InterruptedException {
      if (IS_WINDOWS) {
        String out = exec("ping", "-n", "1", "-w", "2000", host);
        return lastExit == 0 && out.contains("TTL=");
      }
      exec("ping", "-c", "1", "-W", "2", host);
      return lastExit == 0;
    }

    private static void restartInterface() throws Exception {
      if (IS_WINDOWS) {
        List<String> names = windowsInterfaces();
        if (names.isEmpty()) {
          log(
            "Nu s-a gasit nicio interfata activa (folositi --iface \"Nume\")"
          );
        }
        for (String name : names) {
          runLogged(
            "netsh",
            "interface",
            "set",
            "interface",
            "name=" + name,
            "admin=disabled"
          );
          Thread.sleep(3000);
          runLogged(
            "netsh",
            "interface",
            "set",
            "interface",
            "name=" + name,
            "admin=enabled"
          );
        }
      } else {
        runLogged("nmcli", "networking", "off");
        if (lastExit == 0) {
          Thread.sleep(3000);
          runLogged("nmcli", "networking", "on");
        } else {
          log("nmcli networking off a esuat (sunt necesare drepturi de root?)");
        }
      }
      Thread.sleep(10000);
      log("Repornirea interfetei s-a terminat");
    }

    private static List<String> windowsInterfaces()
      throws IOException, InterruptedException
    {
      List<String> names = new ArrayList<>();
      if (iface != null) {
        names.add(iface);
        return names;
      }
      String out = exec("netsh", "interface", "show", "interface");
      boolean data = false;
      for (String line : out.split("\\R")) {
        String t = line.trim();
        if (t.startsWith("---")) {
          data = true;
          continue;
        }
        if (!data || t.isEmpty()) continue;
        String[] p = t.split("\\s+", 4);
        if (
          p.length == 4 &&
          p[0].equalsIgnoreCase("Enabled") &&
          p[2].equalsIgnoreCase("Dedicated")
        ) {
          names.add(p[3]);
        }
      }
      return names;
    }

    private static void install() throws Exception {
      String command = runCommandLine("net", "--run");
      if (!host.equals(DEFAULT_HOST)) command += " --host " + host;
      if (iface != null) command += " --iface \"" + iface + "\"";

      if (IS_WINDOWS) {
        Path bat = APP_DIR.resolve("run.bat");
        Files.createDirectories(APP_DIR);
        Files.write(
          bat,
          (
            "@echo off" +
            System.lineSeparator() +
            command +
            System.lineSeparator()
          ).getBytes()
        );
        runLogged(
          "schtasks",
          "/create",
          "/tn",
          TASK_NAME,
          "/tr",
          bat.toString(),
          "/sc",
          "ONSTART",
          "/ru",
          "SYSTEM",
          "/f"
        );
      } else {
        String job = "@reboot " + command + " # " + TASK_NAME;
        runLogged(
          "sh",
          "-c",
          "(crontab -l 2>/dev/null | grep -v '# " +
            TASK_NAME +
            "'; echo '" +
            job +
            "') | crontab -"
        );
      }
      if (lastExit == 0) log("Pornirea automata a fost configurata");
      else log(
        "EROARE: pornirea automata nu a fost configurata (cod " + lastExit + ")"
      );
    }

    private static void uninstall() throws Exception {
      if (IS_WINDOWS) {
        runLogged("schtasks", "/end", "/tn", TASK_NAME);
        runLogged("schtasks", "/delete", "/tn", TASK_NAME, "/f");
        Files.deleteIfExists(APP_DIR.resolve("run.bat"));
      } else {
        runLogged(
          "sh",
          "-c",
          "crontab -l 2>/dev/null | grep -v '# " + TASK_NAME + "' | crontab -"
        );
      }
      log("Pornirea automata a fost eliminata");
    }

    private static void runLogged(String... cmd)
      throws IOException, InterruptedException
    {
      String out = exec(cmd);
      log("> " + String.join(" ", cmd) + " (cod " + lastExit + ")");
      if (lastExit != 0) log(out.trim());
      else System.out.print(out);
    }

    private static void log(String message) {
      Lab5.log(LOG_FILE, APP_DIR, message);
    }
  }
}
