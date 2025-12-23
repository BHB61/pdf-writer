package de.hft_stuttgart.ip1;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TableRenderer {
    public static TableLayout renderTable(
            PdfContext context,
            float originX,
            float originY,
            int columns,
            int rows,
            List<Float> widths,
            List<Float> heights,
            Color lineColor,
            Color background,
            float thickness
    ) throws IOException {
        if (columns <= 0 || rows <= 0) {
            throw new IOException("Table requires columns and rows");
        }
        List<Float> columnWidths = expandList(widths, columns, 40f);
        List<Float> rowHeights = expandList(heights, rows, 20f);
        TableLayout layout = new TableLayout(
                originX,
                originY,
                columns,
                rows,
                columnWidths,
                rowHeights,
                lineColor,
                background,
                thickness
        );

        PDPageContentStream contentStream = context.getContentStream();
        float totalWidth = columnWidths.stream().reduce(0f, Float::sum);
        float totalHeight = rowHeights.stream().reduce(0f, Float::sum);
        float topY = context.toPdfY(originY);
        float bottomY = topY - totalHeight;

        contentStream.setNonStrokingColor(background);
        contentStream.addRect(originX, bottomY, totalWidth, totalHeight);
        contentStream.fill();

        contentStream.setStrokingColor(lineColor);
        contentStream.setLineWidth(thickness);

        float currentX = originX;
        for (int col = 0; col <= columns; col++) {
            float drawX = currentX;
            contentStream.moveTo(drawX, bottomY);
            contentStream.lineTo(drawX, topY);
            if (col < columns) {
                currentX += columnWidths.get(col);
            }
        }

        float currentY = topY;
        for (int row = 0; row <= rows; row++) {
            float drawY = currentY;
            contentStream.moveTo(originX, drawY);
            contentStream.lineTo(originX + totalWidth, drawY);
            if (row < rows) {
                currentY -= rowHeights.get(row);
            }
        }

        contentStream.stroke();
        return layout;
    }

    private static List<Float> expandList(List<Float> values, int target, float fallback) {
        List<Float> result = new ArrayList<>();
        if (values.isEmpty()) {
            for (int i = 0; i < target; i++) {
                result.add(fallback);
            }
            return result;
        }
        for (int i = 0; i < target; i++) {
            int index = Math.min(i, values.size() - 1);
            result.add(values.get(index));
        }
        return result;
    }
}
