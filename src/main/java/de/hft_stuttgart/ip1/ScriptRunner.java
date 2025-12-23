package de.hft_stuttgart.ip1;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ScriptRunner {
    public Path run(String script, Path baseDir) throws IOException {
        ScriptParser parser = new ScriptParser(script);
        List<ScriptCommand> commands = parser.parse();
        try (PdfContext context = new PdfContext(baseDir)) {
            for (ScriptCommand command : commands) {
                command.execute(context);
            }
            return context.finish();
        }
    }
}
