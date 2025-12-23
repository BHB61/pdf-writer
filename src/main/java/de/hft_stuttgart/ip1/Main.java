package de.hft_stuttgart.ip1;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            Path scriptPath = Path.of(args[0]);
            String script = Files.readString(scriptPath);
            ScriptRunner runner = new ScriptRunner();
            runner.run(script, scriptPath.getParent());
            return;
        }

        EventQueue.invokeLater(() -> {
            ScriptEditor editor = new ScriptEditor();
            editor.setVisible(true);
        });
    }
}
