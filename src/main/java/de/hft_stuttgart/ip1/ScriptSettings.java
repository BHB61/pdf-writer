package de.hft_stuttgart.ip1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ScriptSettings {
    private static final String VIEWER_COMMAND = "viewer.command";
    private final Properties properties = new Properties();
    private final Path propertiesPath = Path.of("pdfscript.properties");

    public ScriptSettings() {
        load();
    }

    public String getViewerCommand() {
        return properties.getProperty(VIEWER_COMMAND, defaultViewerCommand());
    }

    public void setViewerCommand(String command) {
        properties.setProperty(VIEWER_COMMAND, command);
        save();
    }

    public void openPdf(Path pdfPath) throws IOException {
        String command = getViewerCommand();
        String resolved = command.replace("{file}", pdfPath.toAbsolutePath().toString());
        String[] parts = resolved.split("\\s+");
        new ProcessBuilder(parts).start();
    }

    private void load() {
        if (Files.exists(propertiesPath)) {
            try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
                properties.load(inputStream);
            } catch (IOException ignored) {
                // Ignore config errors; defaults will be used.
            }
        }
    }

    private void save() {
        try (OutputStream outputStream = Files.newOutputStream(propertiesPath)) {
            properties.store(outputStream, "PDF Script Settings");
        } catch (IOException ignored) {
            // Ignore write errors.
        }
    }

    private String defaultViewerCommand() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return "cmd /c start {file}";
        }
        if (os.contains("mac")) {
            return "open {file}";
        }
        return "xdg-open {file}";
    }
}
