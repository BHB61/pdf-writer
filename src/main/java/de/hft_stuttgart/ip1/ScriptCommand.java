package de.hft_stuttgart.ip1;

import java.io.IOException;

public interface ScriptCommand {
    void execute(PdfContext context) throws IOException;
}
