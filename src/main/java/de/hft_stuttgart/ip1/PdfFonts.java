package de.hft_stuttgart.ip1;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class PdfFonts {
    public record FontSelection(PDFont font) {
    }

    public static FontSelection resolve(String name, String style) {
        String normalized = name.toLowerCase();
        boolean bold = style.contains("bold");
        boolean italic = style.contains("italic");

        if (normalized.contains("times")) {
            return new FontSelection(selectTimes(bold, italic));
        }
        if (normalized.contains("courier")) {
            return new FontSelection(selectCourier(bold, italic));
        }
        return new FontSelection(selectHelvetica(bold, italic));
    }

    private static PDFont selectHelvetica(boolean bold, boolean italic) {
        if (bold && italic) {
            return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);
        }
        if (bold) {
            return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        }
        if (italic) {
            return new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
        }
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private static PDFont selectTimes(boolean bold, boolean italic) {
        if (bold && italic) {
            return new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD_ITALIC);
        }
        if (bold) {
            return new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
        }
        if (italic) {
            return new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);
        }
        return new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
    }

    private static PDFont selectCourier(boolean bold, boolean italic) {
        if (bold && italic) {
            return new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD_OBLIQUE);
        }
        if (bold) {
            return new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);
        }
        if (italic) {
            return new PDType1Font(Standard14Fonts.FontName.COURIER_OBLIQUE);
        }
        return new PDType1Font(Standard14Fonts.FontName.COURIER);
    }
}
