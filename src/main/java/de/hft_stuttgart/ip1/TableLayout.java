package de.hft_stuttgart.ip1;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TableLayout {
    private final float originX;
    private final float originY;
    private final int columns;
    private final int rows;
    private final List<Float> columnWidths;
    private final List<Float> rowHeights;
    private final Color lineColor;
    private final Color backgroundColor;
    private final float lineThickness;

    public TableLayout(
            float originX,
            float originY,
            int columns,
            int rows,
            List<Float> columnWidths,
            List<Float> rowHeights,
            Color lineColor,
            Color backgroundColor,
            float lineThickness
    ) {
        this.originX = originX;
        this.originY = originY;
        this.columns = columns;
        this.rows = rows;
        this.columnWidths = new ArrayList<>(columnWidths);
        this.rowHeights = new ArrayList<>(rowHeights);
        this.lineColor = lineColor;
        this.backgroundColor = backgroundColor;
        this.lineThickness = lineThickness;
    }

    public float getOriginX() {
        return originX;
    }

    public float getOriginY() {
        return originY;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public List<Float> getColumnWidths() {
        return columnWidths;
    }

    public List<Float> getRowHeights() {
        return rowHeights;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public float getLineThickness() {
        return lineThickness;
    }

    public float getCellX(int column) {
        float x = originX;
        for (int i = 0; i < column && i < columnWidths.size(); i++) {
            x += columnWidths.get(i);
        }
        return x;
    }

    public float getCellY(int row) {
        float y = originY;
        for (int i = 0; i < row && i < rowHeights.size(); i++) {
            y += rowHeights.get(i);
        }
        return y;
    }

    public float getCellWidth(int column) {
        return columnWidths.get(Math.min(column, columnWidths.size() - 1));
    }

    public float getCellHeight(int row) {
        return rowHeights.get(Math.min(row, rowHeights.size() - 1));
    }
}
