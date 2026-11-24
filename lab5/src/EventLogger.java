
import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventLogger {

    private final Path logPath;

    private final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventLogger() throws IOException {

        Path logDirectory = Paths.get(
            System.getProperty("user.home"),
            ".so-lab5"
        );

        Files.createDirectories(logDirectory);

        logPath = logDirectory.resolve("folder-events.log");
    }

    public synchronized String log(String event)
            throws IOException {

        String timestamp =
            LocalDateTime.now().format(formatter);

        String entry = "[" + timestamp + "] " + event;

        Files.write(
            logPath,
            (entry + System.lineSeparator())
                .getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );

        return entry;
    }

    public Path getLogPath() {
        return logPath;
    }

    public synchronized String readLogs()
            throws IOException {

        if (!Files.exists(logPath)) {
            return "No events recorded yet.";
        }

        return new String(
            Files.readAllBytes(logPath),
            StandardCharsets.UTF_8
        );
    }
}
