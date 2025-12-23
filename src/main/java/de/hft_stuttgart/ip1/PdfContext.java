package de.hft_stuttgart.ip1;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.awt.*;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

public class PdfContext implements Closeable {
    private final Path baseDir;
    private PDDocument document;
    private PDPage page;
    private PDPageContentStream contentStream;
    private Path outputFile;
    private PDFont font = PDType1Font.HELVETICA;
    private float fontSize = 12f;
    private Color textColor = Color.BLACK;
    private TableLayout tableLayout;
    private float currentX = 0f;
    private float currentY = 0f;
    private PDAcroForm acroForm;

    public PdfContext(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void setOutputFile(Path outputFile) {
        this.outputFile = outputFile;
        if (document == null) {
            document = new PDDocument();
        }
    }

    public Path getOutputFile() {
        return outputFile;
    }

    public PDDocument getDocument() {
        return document;
    }

    public PDAcroForm getOrCreateAcroForm() {
        ensureDocument();
        if (acroForm == null) {
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            acroForm = new PDAcroForm(document);
            catalog.setAcroForm(acroForm);
        }
        return acroForm;
    }

    public PDPage getPage() {
        return page;
    }

    public PDPageContentStream getContentStream() throws IOException {
        ensurePage();
        if (contentStream == null) {
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        }
        return contentStream;
    }

    public void newPage() throws IOException {
        closeContentStream();
        ensureDocument();
        page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        tableLayout = null;
        currentX = 0f;
        currentY = 0f;
    }

    public void setFont(PDFont font, float size, Color color) {
        this.font = font;
        this.fontSize = size;
        this.textColor = color;
    }

    public PDFont getFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Path resolvePath(String path) {
        return baseDir.resolve(path).normalize();
    }

    public void setTableLayout(TableLayout tableLayout) {
        this.tableLayout = tableLayout;
    }

    public TableLayout getTableLayout() {
        return tableLayout;
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    public void setCurrentPosition(float x, float y) {
        currentX = x;
        currentY = y;
    }

    public PDRectangle getPageBox() {
        ensurePage();
        return page.getMediaBox();
    }

    public float toPdfY(float y) {
        return getPageBox().getHeight() - y;
    }

    public Path finish() throws IOException {
        closeContentStream();
        if (outputFile == null) {
            throw new IOException("No output file configured");
        }
        if (document != null) {
            document.save(outputFile.toFile());
        }
        return outputFile;
    }

    private void ensureDocument() {
        if (document == null) {
            document = new PDDocument();
        }
    }

    private void ensurePage() {
        if (page == null) {
            try {
                newPage();
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to create a new PDF page", ex);
            }
        }
    }

    private void closeContentStream() throws IOException {
        if (contentStream != null) {
            contentStream.close();
            contentStream = null;
        }
    }

    @Override
    public void close() throws IOException {
        closeContentStream();
        if (document != null) {
            document.close();
        }
    }
}
