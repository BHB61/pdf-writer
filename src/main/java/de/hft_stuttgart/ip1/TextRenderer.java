package de.hft_stuttgart.ip1;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextRenderer {
    public record RenderMetrics(int lines, float lineHeight) {
    }

    public static RenderMetrics renderText(
            PdfContext context,
            float x,
            float y,
            Float width,
            TextAlignment alignment,
            String text
    ) throws IOException {
        PDFont font = context.getFont();
        float fontSize = context.getFontSize();
        Color color = context.getTextColor();
        PDPageContentStream contentStream = context.getContentStream();

        List<String> lines = wrapLines(font, fontSize, text, width);
        float lineHeight = fontSize * 1.2f;
        float currentY = context.toPdfY(y);

        for (String line : lines) {
            float lineWidth = font.getStringWidth(line) / 1000f * fontSize;
            float alignedX = switch (alignment) {
                case CENTER -> x - lineWidth / 2f;
                case RIGHT -> x - lineWidth;
                default -> x;
            };
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.setNonStrokingColor(color);
            contentStream.newLineAtOffset(alignedX, currentY);
            contentStream.showText(line);
            contentStream.endText();
            currentY -= lineHeight;
        }
        return new RenderMetrics(lines.size(), lineHeight);
    }

    private static List<String> wrapLines(PDFont font, float fontSize, String text, Float width) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\\n", -1);
        for (int p = 0; p < paragraphs.length; p++) {
            String paragraph = paragraphs[p];
            if (width == null) {
                lines.add(paragraph);
            } else {
                String[] words = paragraph.split("\\s+");
                StringBuilder line = new StringBuilder();
                for (String word : words) {
                    String candidate = line.length() == 0 ? word : line + " " + word;
                    float lineWidth = font.getStringWidth(candidate) / 1000f * fontSize;
                    if (lineWidth > width && line.length() > 0) {
                        lines.add(line.toString());
                        line = new StringBuilder(word);
                    } else {
                        line = new StringBuilder(candidate);
                    }
                }
                if (line.length() > 0) {
                    lines.add(line.toString());
                }
            }
            if (p < paragraphs.length - 1) {
                lines.add("");
            }
        }
        return lines;
    }
}
