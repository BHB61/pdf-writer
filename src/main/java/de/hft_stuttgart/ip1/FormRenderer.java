package de.hft_stuttgart.ip1;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FormRenderer {
    private static int fieldCounter = 1;

    public static void renderControl(
            PdfContext context,
            float x,
            float y,
            ControlType type,
            String optionData,
            String content
    ) throws IOException {
        PDAcroForm form = context.getOrCreateAcroForm();
        PDPage page = context.getPage();
        PDRectangle rect = new PDRectangle(x, context.toPdfY(y) - defaultHeight(), defaultWidth(), defaultHeight());

        switch (type) {
            case DROPDOWN -> addDropdown(form, page, rect, optionData, content);
            case RADIO -> addRadio(form, page, rect, optionData);
            case OPTION -> addCheckbox(form, page, rect, content);
            case TEXTBOX -> addTextbox(form, page, rect, content);
            default -> addTextbox(form, page, rect, content);
        }
    }

    private static void addTextbox(PDAcroForm form, PDPage page, PDRectangle rect, String content) throws IOException {
        PDTextField field = new PDTextField(form);
        field.setPartialName("textbox" + fieldCounter++);
        field.setValue(content == null ? "" : content);
        attachWidget(field, page, rect);
        form.getFields().add(field);
    }

    private static void addDropdown(PDAcroForm form, PDPage page, PDRectangle rect, String options, String content) throws IOException {
        PDComboBox field = new PDComboBox(form);
        field.setPartialName("dropdown" + fieldCounter++);
        List<String> values = options == null ? List.of() : Arrays.stream(options.split(";"))
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .toList();
        field.setOptions(values);
        if (content != null) {
            field.setValue(content);
        }
        attachWidget(field, page, rect);
        form.getFields().add(field);
    }

    private static void addRadio(PDAcroForm form, PDPage page, PDRectangle rect, String data) throws IOException {
        String group = "radio" + fieldCounter;
        String value = "option";
        if (data != null) {
            String[] parts = data.split(",");
            if (parts.length > 0) {
                value = parts[0].trim();
            }
            if (parts.length > 1) {
                group = parts[1].trim();
            }
        }
        PDRadioButton field = new PDRadioButton(form);
        field.setPartialName(group);
        field.setValue(value);
        attachWidget(field, page, rect);
        form.getFields().add(field);
    }

    private static void addCheckbox(PDAcroForm form, PDPage page, PDRectangle rect, String content) throws IOException {
        PDCheckBox field = new PDCheckBox(form);
        field.setPartialName("checkbox" + fieldCounter++);
        if ("true".equalsIgnoreCase(content) || "yes".equalsIgnoreCase(content)) {
            field.check();
        }
        attachWidget(field, page, rect);
        form.getFields().add(field);
    }

    private static void attachWidget(PDField field, PDPage page, PDRectangle rect) throws IOException {
        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(rect);
        widget.setPage(page);
        widget.setCOSObject(new COSDictionary());
        field.getWidgets().add(widget);
        page.getAnnotations().add(widget);
    }

    private static float defaultWidth() {
        return 150f;
    }

    private static float defaultHeight() {
        return 20f;
    }
}
