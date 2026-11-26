
import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

public class FolderMonitor implements AutoCloseable {

    private final Path folder;
    private final Consumer<String> listener;

    private WatchService watchService;
    private volatile boolean running;

    public FolderMonitor(Path folder,
                         Consumer<String> listener) {

        this.folder = folder.toAbsolutePath().normalize();
        this.listener = listener;
    }

    public synchronized void start() throws IOException {

        if (running) {
            return;
        }

        if (!Files.isDirectory(folder)) {
            throw new IOException("Invalid folder.");
        }

        WatchService service =
                FileSystems.getDefault().newWatchService();

        try {
            folder.register(
                service,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            );
        } catch (IOException | RuntimeException e) {
            service.close();
            throw e;
        }

        watchService = service;
        running = true;

        Thread thread = new Thread(
            () -> monitor(service),
            "FolderMonitorThread"
        );

        thread.setDaemon(true);
        thread.start();
    }

    private void monitor(WatchService service) {

        try {

            while (running) {

                WatchKey key = service.take();

                for (WatchEvent<?> event : key.pollEvents()) {

                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        listener.accept("WARNING: Events lost.");
                        continue;
                    }

                    Path file = folder.resolve(
                        (Path) event.context()
                    );

                    String action;

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        action = "CREATED";

                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        action = "MODIFIED";

                    } else {
                        action = "DELETED";
                    }

                    listener.accept(action + ": " + file);
                }

                if (!key.reset()) {
                    listener.accept("Folder is no longer accessible.");
                    break;
                }
            }

        } catch (ClosedWatchServiceException e) {
            // Normal shutdown.

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } finally {

            synchronized (this) {
                if (watchService == service) {
                    running = false;
                    watchService = null;
                }
            }

            try {
                service.close();
            } catch (IOException e) {
                listener.accept("Error closing monitor.");
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void close() throws IOException {

        running = false;

        if (watchService != null) {
            watchService.close();
        }
    }
}
