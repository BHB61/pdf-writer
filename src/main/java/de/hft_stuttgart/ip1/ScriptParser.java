package de.hft_stuttgart.ip1;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ScriptParser {
    private final ScriptTokenizer tokenizer;

    public ScriptParser(String script) {
        this.tokenizer = new ScriptTokenizer(script);
    }

    public List<ScriptCommand> parse() throws IOException {
        List<ScriptCommand> commands = new ArrayList<>();
        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.next();
            if (token.type() == TokenType.SYMBOL && ".".equals(token.text())) {
                continue;
            }
            if (token.type() != TokenType.WORD) {
                throw new IOException("Expected command but found: " + token.text());
            }
            String command = token.text().toLowerCase();
            switch (command) {
                case "output" -> commands.add(parseOutput());
                case "font" -> commands.add(parseFont());
                case "print" -> commands.add(parsePrint());
                case "nextpage" -> commands.add(parseNextPage());
                case "image" -> commands.add(parseImage());
                case "table" -> commands.add(parseTable());
                case "control" -> commands.add(parseControl());
                default -> throw new IOException("Unknown command: " + command);
            }
            consumeCommandEnd();
        }
        return commands;
    }

    private ScriptCommand parseOutput() throws IOException {
        String fileName = expectString("output file");
        return context -> context.setOutputFile(context.resolvePath(fileName));
    }

    private ScriptCommand parseFont() throws IOException {
        Float size = null;
        String style = "regular";
        Color color = Color.BLACK;
        String fontName = null;

        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.peek();
            if (token.type() == TokenType.STRING) {
                fontName = tokenizer.next().text();
                break;
            }
            if (token.type() == TokenType.SYMBOL && ".".equals(token.text())) {
                break;
            }
            String keyword = expectWord("font option");
            switch (keyword) {
                case "size" -> size = expectNumber("font size");
                case "style" -> style = expectWord("font style");
                case "colour", "color" -> color = ColorParser.parseColor(expectWordOrNumber("font colour"));
                default -> throw new IOException("Unknown font option: " + keyword);
            }
        }

        if (size == null) {
            throw new IOException("Font size is required");
        }
        String resolvedFontName = fontName == null ? "Helvetica" : fontName;
        String resolvedStyle = style.toLowerCase();
        Color resolvedColor = color;
        return context -> {
            PdfFonts.FontSelection selection = PdfFonts.resolve(resolvedFontName, resolvedStyle);
            context.setFont(selection.font(), size, resolvedColor);
        };
    }

    private ScriptCommand parsePrint() throws IOException {
        Float x = null;
        Float y = null;
        Integer cellX = null;
        Integer cellY = null;
        Float width = null;
        TextAlignment alignment = TextAlignment.LEFT;
        boolean fromCell = false;

        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.peek();
            if (token.type() == TokenType.STRING) {
                break;
            }
            if (token.type() == TokenType.SYMBOL && ".".equals(token.text())) {
                break;
            }
            if (token.type() == TokenType.SYMBOL && "{".equals(token.text())) {
                alignment = parseAlignmentBlock();
                continue;
            }

            String keyword = expectWord("print option");
            if (keyword.startsWith("@") && keyword.length() > 1 && !"@cell".equals(keyword)) {
                x = parseFloat(keyword.substring(1));
                expectSymbol(",");
                y = expectNumber("y");
                continue;
            }
            switch (keyword) {
                case "@" -> {
                    if (peekWord("cell")) {
                        tokenizer.next();
                        cellX = expectInt("cell column");
                        expectSymbol(",");
                        cellY = expectInt("cell row");
                        fromCell = true;
                    } else {
                        x = expectNumber("x");
                        expectSymbol(",");
                        y = expectNumber("y");
                    }
                }
                case "@cell" -> {
                    cellX = expectInt("cell column");
                    expectSymbol(",");
                    cellY = expectInt("cell row");
                    fromCell = true;
                }
                case "cell" -> {
                    cellX = expectInt("cell column");
                    expectSymbol(",");
                    cellY = expectInt("cell row");
                    fromCell = true;
                }
                case "width" -> width = expectNumber("width");
                case "alignment", "aligmnent" -> alignment = TextAlignment.from(expectWord("alignment"));
                default -> throw new IOException("Unknown print option: " + keyword);
            }
        }

        String text = expectString("print text");
        Float resolvedX = x;
        Float resolvedY = y;
        Float resolvedWidth = width;
        Integer resolvedCellX = cellX;
        Integer resolvedCellY = cellY;
        TextAlignment resolvedAlignment = alignment;
        boolean resolvedFromCell = fromCell;

        return context -> {
            Position position = resolvePosition(context, resolvedX, resolvedY, resolvedCellX, resolvedCellY);
            Float resolvedCellWidth = resolvedWidth;
            if (resolvedCellWidth == null && resolvedFromCell) {
                TableLayout layout = context.getTableLayout();
                if (layout != null && resolvedCellX != null) {
                    resolvedCellWidth = layout.getCellWidth(resolvedCellX);
                }
            }
            TextRenderer.RenderMetrics metrics = TextRenderer.renderText(
                    context,
                    position.x(),
                    position.y(),
                    resolvedCellWidth,
                    resolvedAlignment,
                    text
            );
            context.setCurrentPosition(position.x(), position.y() + metrics.lineHeight() * metrics.lines());
        };
    }

    private ScriptCommand parseNextPage() {
        return PdfContext::newPage;
    }

    private ScriptCommand parseImage() throws IOException {
        Float x = null;
        Float y = null;
        Float width = null;
        Float height = null;

        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.peek();
            if (token.type() == TokenType.STRING) {
                break;
            }
            if (token.type() == TokenType.SYMBOL && ".".equals(token.text())) {
                break;
            }
            String keyword = expectWord("image option");
            if (keyword.startsWith("@") && keyword.length() > 1) {
                x = parseFloat(keyword.substring(1));
                expectSymbol(",");
                y = expectNumber("y");
                continue;
            }
            switch (keyword) {
                case "@" -> {
                    x = expectNumber("x");
                    expectSymbol(",");
                    y = expectNumber("y");
                }
                case "size" -> {
                    width = expectNumber("width");
                    expectSymbol(",");
                    height = expectNumber("height");
                }
                default -> throw new IOException("Unknown image option: " + keyword);
            }
        }

        String path = expectString("image path");
        Float resolvedX = x;
        Float resolvedY = y;
        Float resolvedWidth = width;
        Float resolvedHeight = height;

        return context -> ImageRenderer.renderImage(context, resolvedX, resolvedY, resolvedWidth, resolvedHeight, path);
    }

    private ScriptCommand parseTable() throws IOException {
        int columns = 0;
        int rows = 0;
        List<Float> widths = new ArrayList<>();
        List<Float> heights = new ArrayList<>();
        Color lineColor = Color.BLACK;
        Color background = Color.WHITE;
        float thickness = 2f;
        Float originX = null;
        Float originY = null;

        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.peek();
            if (token.type() == TokenType.SYMBOL && ".".equals(token.text())) {
                break;
            }
            if (token.type() == TokenType.STRING) {
                break;
            }
            String keyword = expectWord("table option");
            if (keyword.startsWith("@") && keyword.length() > 1) {
                originX = parseFloat(keyword.substring(1));
                expectSymbol(",");
                originY = expectNumber("origin y");
                continue;
            }
            switch (keyword) {
                case "columns" -> columns = expectInt("columns");
                case "rows" -> rows = expectInt("rows");
                case "width" -> widths = parseNumberList();
                case "height" -> heights = parseNumberList();
                case "lines" -> lineColor = ColorParser.parseColor(expectWordOrNumber("line color"));
                case "background" -> background = ColorParser.parseColor(expectWordOrNumber("background color"));
                case "thickness" -> thickness = expectNumber("thickness");
                case "@" -> {
                    originX = expectNumber("origin x");
                    expectSymbol(",");
                    originY = expectNumber("origin y");
                }
                default -> throw new IOException("Unknown table option: " + keyword);
            }
        }

        int resolvedColumns = columns;
        int resolvedRows = rows;
        List<Float> resolvedWidths = widths;
        List<Float> resolvedHeights = heights;
        Color resolvedLines = lineColor;
        Color resolvedBackground = background;
        float resolvedThickness = thickness;
        float resolvedX = originX;
        float resolvedY = originY;

        return context -> {
            float resolvedOriginX = originX == null ? context.getCurrentX() : originX;
            float resolvedOriginY = originY == null ? context.getCurrentY() : originY;
            TableLayout layout = TableRenderer.renderTable(
                    context,
                    resolvedOriginX,
                    resolvedOriginY,
                    resolvedColumns,
                    resolvedRows,
                    resolvedWidths,
                    resolvedHeights,
                    resolvedLines,
                    resolvedBackground,
                    resolvedThickness
            );
            context.setTableLayout(layout);
            context.setCurrentPosition(resolvedOriginX, resolvedOriginY);
        };
    }

    private ScriptCommand parseControl() throws IOException {
        Float x = null;
        Float y = null;
        Integer cellX = null;
        Integer cellY = null;
        String content = null;
        ControlType type = null;
        String optionData = null;

        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.peek();
            if (token.type() == TokenType.SYMBOL && ".".equals(token.text())) {
                break;
            }
            if (token.type() == TokenType.SYMBOL && "{".equals(token.text())) {
                type = parseControlBlock();
                continue;
            }
            String keyword = expectWord("control option");
            if (keyword.startsWith("@") && keyword.length() > 1 && !"@cell".equals(keyword)) {
                x = parseFloat(keyword.substring(1));
                expectSymbol(",");
                y = expectNumber("y");
                continue;
            }
            switch (keyword) {
                case "@" -> {
                    if (peekWord("cell")) {
                        tokenizer.next();
                        cellX = expectInt("cell column");
                        expectSymbol(",");
                        cellY = expectInt("cell row");
                    } else {
                        x = expectNumber("x");
                        expectSymbol(",");
                        y = expectNumber("y");
                    }
                }
                case "@cell" -> {
                    cellX = expectInt("cell column");
                    expectSymbol(",");
                    cellY = expectInt("cell row");
                }
                case "content" -> content = expectString("content");
                case "type" -> {
                    String typeName = expectWord("control type");
                    if ("dropdown".equals(typeName) || "radio".equals(typeName)) {
                        if (tokenizer.peek().type() == TokenType.STRING) {
                            optionData = tokenizer.next().text();
                        } else if (tokenizer.peek().type() == TokenType.WORD) {
                            optionData = tokenizer.next().text();
                        }
                    }
                    type = ControlType.from(typeName, optionData);
                }
                default -> throw new IOException("Unknown control option: " + keyword);
            }
        }
        Float resolvedX = x;
        Float resolvedY = y;
        Integer resolvedCellX = cellX;
        Integer resolvedCellY = cellY;
        String resolvedContent = content;
        ControlType resolvedType = type == null ? ControlType.TEXTBOX : type;
        String resolvedData = optionData;

        return context -> {
            Position position = resolvePosition(context, resolvedX, resolvedY, resolvedCellX, resolvedCellY);
            FormRenderer.renderControl(context, position.x(), position.y(), resolvedType, resolvedData, resolvedContent);
        };
    }

    private List<Float> parseNumberList() throws IOException {
        List<Float> values = new ArrayList<>();
        boolean repeat = false;
        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.peek();
            if (token.type() == TokenType.SYMBOL && ".".equals(token.text())) {
                break;
            }
            if (token.type() == TokenType.NUMBER) {
                values.add(expectNumber("number"));
                if (tokenizer.peek().type() == TokenType.SYMBOL && "*".equals(tokenizer.peek().text())) {
                    tokenizer.next();
                    repeat = true;
                    break;
                }
                if (tokenizer.peek().type() == TokenType.SYMBOL && ",".equals(tokenizer.peek().text())) {
                    tokenizer.next();
                    continue;
                }
                break;
            } else {
                break;
            }
        }
        if (repeat && !values.isEmpty()) {
            values.add(values.get(values.size() - 1));
        }
        return values;
    }

    private void consumeCommandEnd() throws IOException {
        if (tokenizer.peek().type() == TokenType.SYMBOL && ".".equals(tokenizer.peek().text())) {
            tokenizer.next();
        }
    }

    private String expectString(String label) throws IOException {
        Token token = tokenizer.next();
        if (token.type() != TokenType.STRING) {
            throw new IOException("Expected string for " + label + " but got " + token.text());
        }
        return token.text();
    }

    private String expectWord(String label) throws IOException {
        Token token = tokenizer.next();
        if (token.type() != TokenType.WORD && token.type() != TokenType.SYMBOL) {
            throw new IOException("Expected word for " + label + " but got " + token.text());
        }
        return token.text().toLowerCase();
    }

    private String expectWordOrNumber(String label) throws IOException {
        Token token = tokenizer.next();
        if (token.type() == TokenType.WORD || token.type() == TokenType.NUMBER) {
            return token.text();
        }
        throw new IOException("Expected word for " + label + " but got " + token.text());
    }

    private float expectNumber(String label) throws IOException {
        Token token = tokenizer.next();
        if (token.type() != TokenType.NUMBER) {
            throw new IOException("Expected number for " + label + " but got " + token.text());
        }
        return parseFloat(token.text());
    }

    private int expectInt(String label) throws IOException {
        return Math.round(expectNumber(label));
    }

    private void expectSymbol(String symbol) throws IOException {
        Token token = tokenizer.next();
        if (token.type() != TokenType.SYMBOL || !symbol.equals(token.text())) {
            throw new IOException("Expected symbol '" + symbol + "' but got " + token.text());
        }
    }

    private float parseFloat(String text) throws IOException {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid number: " + text);
        }
    }

    private Position resolvePosition(
            PdfContext context,
            Float x,
            Float y,
            Integer cellX,
            Integer cellY
    ) throws IOException {
        if (cellX != null && cellY != null) {
            TableLayout layout = context.getTableLayout();
            if (layout == null) {
                throw new IOException("No table defined for cell positioning");
            }
            float padding = 2f;
            float cellOriginX = layout.getCellX(cellX) + padding;
            float cellOriginY = layout.getCellY(cellY) + padding;
            return new Position(cellOriginX, cellOriginY);
        }
        if (x == null || y == null) {
            return new Position(context.getCurrentX(), context.getCurrentY());
        }
        return new Position(x, y);
    }

    private boolean peekWord(String expected) throws IOException {
        Token token = tokenizer.peek();
        return token.type() == TokenType.WORD && expected.equalsIgnoreCase(token.text());
    }

    private TextAlignment parseAlignmentBlock() throws IOException {
        expectSymbol("{");
        String alignment = expectWord("alignment");
        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.peek();
            if (token.type() == TokenType.SYMBOL && "}".equals(token.text())) {
                tokenizer.next();
                break;
            }
            tokenizer.next();
        }
        return TextAlignment.from(alignment);
    }

    private ControlType parseControlBlock() throws IOException {
        expectSymbol("{");
        String typeName = expectWord("control type");
        while (tokenizer.peek().type() != TokenType.EOF) {
            Token token = tokenizer.peek();
            if (token.type() == TokenType.SYMBOL && "}".equals(token.text())) {
                tokenizer.next();
                break;
            }
            tokenizer.next();
        }
        return ControlType.from(typeName, null);
    }
}
