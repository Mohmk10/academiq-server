package com.academiq.service.pdf;

import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.NiveauRepository;
import com.academiq.repository.PromotionRepository;
import com.academiq.service.BulletinService;
import com.academiq.service.CalculService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    private final BulletinService bulletinService;
    private final CalculService calculService;
    private final EtudiantRepository etudiantRepository;
    private final PromotionRepository promotionRepository;
    private final InscriptionRepository inscriptionRepository;
    private final NiveauRepository niveauRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);

    void ajouterEnTete(Document document, String titre) throws DocumentException {
        Paragraph logo = new Paragraph("AcademiQ", PdfStyleHelper.getTitleFont());
        logo.setAlignment(Element.ALIGN_CENTER);
        document.add(logo);

        Paragraph sousTitre = new Paragraph("Plateforme de Gestion Académique",
                PdfStyleHelper.getSmallFont());
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        document.add(sousTitre);

        PdfStyleHelper.addEmptyLine(document, 1);

        Paragraph ligne = new Paragraph(" ");
        ligne.setSpacingAfter(2);
        document.add(ligne);

        com.lowagie.text.pdf.PdfPTable separateur = new com.lowagie.text.pdf.PdfPTable(1);
        separateur.setWidthPercentage(100);
        com.lowagie.text.pdf.PdfPCell cellSep = new com.lowagie.text.pdf.PdfPCell();
        cellSep.setBorderWidthTop(2);
        cellSep.setBorderWidthBottom(0);
        cellSep.setBorderWidthLeft(0);
        cellSep.setBorderWidthRight(0);
        cellSep.setBorderColorTop(new Color(0, 51, 102));
        cellSep.setFixedHeight(3);
        separateur.addCell(cellSep);
        document.add(separateur);

        PdfStyleHelper.addEmptyLine(document, 1);

        Paragraph titreDoc = new Paragraph(titre,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD, new Color(0, 51, 102)));
        titreDoc.setAlignment(Element.ALIGN_CENTER);
        document.add(titreDoc);

        Paragraph dateGen = new Paragraph(
                "Généré le " + LocalDate.now().format(DATE_FORMATTER),
                PdfStyleHelper.getSmallFont());
        dateGen.setAlignment(Element.ALIGN_CENTER);
        document.add(dateGen);

        PdfStyleHelper.addEmptyLine(document, 1);
    }

    void ajouterPiedDePage(PdfWriter writer) {
        writer.setPageEvent(new PiedDePageEvent());
    }

    static class PiedDePageEvent extends PdfPageEventHelper {

        private PdfTemplate totalPages;
        private final Font footerFont = FontFactory.getFont(
                FontFactory.HELVETICA, 8, Font.NORMAL, Color.GRAY);

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPages = writer.getDirectContent().createTemplate(30, 16);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            String gauche = "AcademiQ — Document généré le "
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(gauche, footerFont),
                    document.leftMargin(), 25, 0);

            String droite = "Page " + writer.getPageNumber() + " / ";
            float textWidth = footerFont.getCalculatedBaseFont(false)
                    .getWidthPoint(droite, footerFont.getSize());
            float xPos = document.right() - textWidth - 20;

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(droite, footerFont),
                    xPos, 25, 0);

            cb.addTemplate(totalPages, xPos + textWidth, 25);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPages.beginText();
            totalPages.setFontAndSize(
                    footerFont.getCalculatedBaseFont(false), footerFont.getSize());
            totalPages.showText(String.valueOf(writer.getPageNumber() - 1));
            totalPages.endText();
        }
    }
}
