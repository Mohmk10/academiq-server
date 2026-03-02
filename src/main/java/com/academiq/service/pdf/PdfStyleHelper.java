package com.academiq.service.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;

public final class PdfStyleHelper {

    private static final Color COULEUR_PRIMAIRE = new Color(0, 51, 102);
    private static final Color COULEUR_SECONDAIRE = new Color(240, 240, 240);
    private static final Color COULEUR_HEADER_TABLEAU = new Color(0, 102, 153);
    private static final Color COULEUR_TEXTE_HEADER = Color.WHITE;

    private PdfStyleHelper() {
    }

    public static Color getCouleurSecondaire() {
        return COULEUR_SECONDAIRE;
    }

    public static Font getTitleFont() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, COULEUR_PRIMAIRE);
    }

    public static Font getSubtitleFont() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, COULEUR_PRIMAIRE);
    }

    public static Font getHeaderFont() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, COULEUR_TEXTE_HEADER);
    }

    public static Font getNormalFont() {
        return FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    }

    public static Font getBoldFont() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.BLACK);
    }

    public static Font getSmallFont() {
        return FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.GRAY);
    }

    public static PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, getHeaderFont()));
        cell.setBackgroundColor(COULEUR_HEADER_TABLEAU);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    public static PdfPCell createDataCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, getNormalFont()));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    public static PdfPCell createDataCellCentered(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, getNormalFont()));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    public static void addEmptyLine(Document document, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            document.add(new Paragraph(" "));
        }
    }

    public static PdfPTable createStyledTable(int numColumns, float[] widths) throws DocumentException {
        PdfPTable table = new PdfPTable(numColumns);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        return table;
    }
}
