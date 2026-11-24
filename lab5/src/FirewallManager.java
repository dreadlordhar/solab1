
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Locale;

public class FirewallManager {

    private static final String RULE_NAME = "SO_Lab5_8080";
    private static final String PORT = "8080";

    private final String os = System.getProperty("os.name")
            .toLowerCase(Locale.ROOT);

    public String addRule()
            throws IOException, InterruptedException {

        if (os.startsWith("windows")) {

            return execute(
                "netsh", "advfirewall", "firewall",
                "add", "rule",
                "name=" + RULE_NAME,
                "dir=in",
                "action=allow",
                "protocol=TCP",
                "localport=" + PORT,
                "profile=private",
                "remoteip=LocalSubnet"
            );

        } else if (os.contains("linux")) {

            return execute(
                "iptables", "-I", "INPUT",
                "-p", "tcp",
                "--dport", PORT,
                "-m", "comment",
                "--comment", RULE_NAME,
                "-j", "ACCEPT"
            );
        }

        throw new IOException("Unsupported operating system.");
    }

    public String removeRule()
            throws IOException, InterruptedException {

        if (os.startsWith("windows")) {

            return execute(
                "netsh", "advfirewall", "firewall",
                "delete", "rule",
                "name=" + RULE_NAME
            );

        } else if (os.contains("linux")) {

            return execute(
                "iptables", "-D", "INPUT",
                "-p", "tcp",
                "--dport", PORT,
                "-m", "comment",
                "--comment", RULE_NAME,
                "-j", "ACCEPT"
            );
        }

        throw new IOException("Unsupported operating system.");
    }

    private String execute(String... command)
            throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder(command);
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

        if (exitCode != 0) {
            throw new IOException(
                "Command failed. Exit code: " + exitCode
                + "\n" + result
            );
        }

        return result.toString();
    }
}
