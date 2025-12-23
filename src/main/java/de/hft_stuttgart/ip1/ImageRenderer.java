package de.hft_stuttgart.ip1;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.nio.file.Path;

public class ImageRenderer {
    public static void renderImage(
            PdfContext context,
            Float x,
            Float y,
            Float width,
            Float height,
            String path
    ) throws IOException {
        if (x == null || y == null) {
            throw new IOException("Image requires a position");
        }
        Path resolved = context.resolvePath(path);
        PDImageXObject image = PDImageXObject.createFromFile(resolved.toString(), context.getDocument());
        float drawWidth = width == null ? image.getWidth() : width;
        float drawHeight = height == null ? image.getHeight() : height;
        PDPageContentStream contentStream = context.getContentStream();
        contentStream.drawImage(image, x, context.toPdfY(y) - drawHeight, drawWidth, drawHeight);
    }
}
