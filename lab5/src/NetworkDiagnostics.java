
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Locale;

public class NetworkDiagnostics {

    private final boolean windows =
        System.getProperty("os.name")
        .toLowerCase(Locale.ROOT)
        .startsWith("windows");

    public String ping(String host)
            throws IOException, InterruptedException {

        validateHost(host);

        return execute(
            "ping", windows ? "-n" : "-c", "4", host
        );
    }

    public String traceroute(String host)
            throws IOException, InterruptedException {

        validateHost(host);

        return execute(
            windows ? "tracert" : "traceroute", host
        );
    }

    public String ipConfig()
            throws IOException, InterruptedException {

        if (windows) {
            return execute("ipconfig", "/all");
        }

        return execute("ip", "addr", "show");
    }

    public String checkPort(String host, int port) {

        validateHost(host);

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port.");
        }

        try (Socket socket = new Socket()) {

            socket.connect(
                new InetSocketAddress(host, port), 3000
            );

            return "Port " + port + " on " + host
                + " is reachable.";

        } catch (IOException e) {

            return "Port " + port + " on " + host
                + " is not reachable: " + e.getMessage();
        }
    }

    private void validateHost(String host) {

        if (host == null
                || !host.matches("[a-zA-Z0-9][a-zA-Z0-9.:-]*")) {

            throw new IllegalArgumentException(
                "Invalid IP address or hostname."
            );
        }
    }

    private String execute(String... command)
            throws IOException, InterruptedException {

        ProcessBuilder builder =
            new ProcessBuilder(command);

        builder.redirectErrorStream(true);

        Process process = builder.start();

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        result.append("\nExit code: ").append(exitCode);

        return result.toString();
    }
}
