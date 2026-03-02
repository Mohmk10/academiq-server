package com.academiq.service.pdf;

import com.academiq.dto.calcul.BulletinEtudiantDTO;
import com.academiq.dto.calcul.BulletinModuleDTO;
import com.academiq.dto.calcul.BulletinSemestreDTO;
import com.academiq.dto.calcul.BulletinUeDTO;
import com.academiq.entity.DecisionJury;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Promotion;
import com.academiq.entity.Utilisateur;
import com.academiq.exception.BadRequestException;
import com.academiq.exception.ResourceNotFoundException;
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
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
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

    public byte[] genererReleve(Long etudiantId, Long promotionId) {
        BulletinEtudiantDTO bulletin = bulletinService.genererBulletin(etudiantId, promotionId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            ajouterPiedDePage(writer);
            document.open();

            ajouterEnTete(document, "RELEVÉ DE NOTES");

            ajouterBlocInfosEtudiant(document, bulletin);

            for (BulletinSemestreDTO semestre : bulletin.getSemestres()) {
                ajouterSemestre(document, semestre, bulletin);
            }

            ajouterRecapitulatifFinal(document, bulletin);
            ajouterZoneSignature(document);

            document.close();
            log.info("Relevé de notes généré pour l'étudiant {}", etudiantId);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du relevé de notes", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    public byte[] genererAttestation(Long etudiantId, Long promotionId) {
        BulletinEtudiantDTO bulletin = bulletinService.genererBulletin(etudiantId, promotionId);

        String decision = bulletin.getDecision();
        if (!DecisionJury.ADMIS.name().equals(decision)
                && !DecisionJury.ADMIS_COMPENSATION.name().equals(decision)) {
            throw new BadRequestException("L'étudiant n'est pas admis, attestation impossible");
        }

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));
        Utilisateur utilisateur = etudiant.getUtilisateur();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 60, 60, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            ajouterPiedDePage(writer);
            document.open();

            Paragraph institut = new Paragraph("NATIONAL INSTITUTE OF INFORMATION TECHNOLOGY",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, new Color(0, 51, 102)));
            institut.setAlignment(Element.ALIGN_CENTER);
            document.add(institut);

            Paragraph niit = new Paragraph("NIIT SÉNÉGAL",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, new Color(0, 51, 102)));
            niit.setAlignment(Element.ALIGN_CENTER);
            document.add(niit);

            PdfStyleHelper.addEmptyLine(document, 1);

            PdfPTable separateur = new PdfPTable(1);
            separateur.setWidthPercentage(100);
            PdfPCell cellSep = new PdfPCell();
            cellSep.setBorderWidthTop(2);
            cellSep.setBorderWidthBottom(0);
            cellSep.setBorderWidthLeft(0);
            cellSep.setBorderWidthRight(0);
            cellSep.setBorderColorTop(new Color(0, 51, 102));
            cellSep.setFixedHeight(3);
            separateur.addCell(cellSep);
            document.add(separateur);

            PdfStyleHelper.addEmptyLine(document, 2);

            Paragraph titre = new Paragraph("ATTESTATION DE RÉUSSITE",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, new Color(0, 51, 102)));
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);

            PdfStyleHelper.addEmptyLine(document, 2);

            String dateNaissance = utilisateur.getDateNaissance() != null
                    ? utilisateur.getDateNaissance().format(DATE_FORMATTER)
                    : "non renseignée";

            String corps = String.format(
                    "Le Directeur des Études de NIIT Sénégal atteste que l'étudiant(e) %s %s, " +
                    "matricule %s, né(e) le %s a satisfait aux conditions requises pour la validation " +
                    "du niveau %s de la filière %s au titre de l'année universitaire %s.",
                    utilisateur.getPrenom(),
                    utilisateur.getNom().toUpperCase(),
                    etudiant.getMatricule(),
                    dateNaissance,
                    bulletin.getNiveauNom(),
                    bulletin.getFiliereNom(),
                    bulletin.getAnneeUniversitaire());

            Paragraph paragrapheCorps = new Paragraph(corps, PdfStyleHelper.getNormalFont());
            paragrapheCorps.setAlignment(Element.ALIGN_JUSTIFIED);
            paragrapheCorps.setLeading(18);
            document.add(paragrapheCorps);

            PdfStyleHelper.addEmptyLine(document, 1);

            Paragraph titreResultats = new Paragraph("Résultats :", PdfStyleHelper.getBoldFont());
            titreResultats.setSpacingAfter(5);
            document.add(titreResultats);

            Paragraph moyenneLine = new Paragraph(
                    "      Moyenne générale : " + formatNote(bulletin.getMoyenneAnnuelle()) + " / 20",
                    PdfStyleHelper.getNormalFont());
            document.add(moyenneLine);

            if (bulletin.getMention() != null) {
                Paragraph mentionLine = new Paragraph(
                        "      Mention : " + bulletin.getMention(), PdfStyleHelper.getNormalFont());
                document.add(mentionLine);
            }

            Paragraph creditsLine = new Paragraph(
                    "      Crédits ECTS validés : " + bulletin.getCreditsValides()
                            + " / " + bulletin.getCreditsTotaux(),
                    PdfStyleHelper.getNormalFont());
            document.add(creditsLine);

            PdfStyleHelper.addEmptyLine(document, 1);

            Paragraph cloture = new Paragraph(
                    "En foi de quoi, la présente attestation lui est délivrée pour servir et valoir ce que de droit.",
                    PdfStyleHelper.getNormalFont());
            cloture.setAlignment(Element.ALIGN_JUSTIFIED);
            cloture.setLeading(18);
            document.add(cloture);

            PdfStyleHelper.addEmptyLine(document, 2);

            Paragraph lieu = new Paragraph(
                    "Fait à Dakar, le " + LocalDate.now().format(DATE_FORMATTER),
                    PdfStyleHelper.getNormalFont());
            lieu.setAlignment(Element.ALIGN_RIGHT);
            document.add(lieu);

            PdfStyleHelper.addEmptyLine(document, 3);

            Paragraph signataire = new Paragraph("Le Directeur des Études",
                    PdfStyleHelper.getBoldFont());
            signataire.setAlignment(Element.ALIGN_RIGHT);
            document.add(signataire);

            PdfStyleHelper.addEmptyLine(document, 1);

            Paragraph ligneSignature = new Paragraph("_________________________",
                    PdfStyleHelper.getNormalFont());
            ligneSignature.setAlignment(Element.ALIGN_RIGHT);
            document.add(ligneSignature);

            PdfStyleHelper.addEmptyLine(document, 2);

            Paragraph avertissement = new Paragraph(
                    "Ce document ne remplace pas le diplôme officiel.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Font.ITALIC, Color.GRAY));
            avertissement.setAlignment(Element.ALIGN_CENTER);
            document.add(avertissement);

            document.close();
            log.info("Attestation de réussite générée pour l'étudiant {}", etudiantId);
            return baos.toByteArray();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'attestation", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private void ajouterBlocInfosEtudiant(Document document, BulletinEtudiantDTO bulletin)
            throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});

        ajouterInfoLigne(table, "Nom", bulletin.getEtudiantNom());
        ajouterInfoLigne(table, "Prénom", bulletin.getEtudiantPrenom());
        ajouterInfoLigne(table, "Matricule", bulletin.getEtudiantMatricule());
        ajouterInfoLigne(table, "Filière", bulletin.getFiliereNom());
        ajouterInfoLigne(table, "Niveau", bulletin.getNiveauNom());
        ajouterInfoLigne(table, "Promotion", bulletin.getPromotionNom());
        ajouterInfoLigne(table, "Année universitaire", bulletin.getAnneeUniversitaire());

        document.add(table);
        PdfStyleHelper.addEmptyLine(document, 1);
    }

    private void ajouterInfoLigne(PdfPTable table, String label, String valeur) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label + " :", PdfStyleHelper.getBoldFont()));
        cellLabel.setBorder(0);
        cellLabel.setPadding(3);
        table.addCell(cellLabel);

        PdfPCell cellValeur = new PdfPCell(new Phrase(
                valeur != null ? valeur : "N/A", PdfStyleHelper.getNormalFont()));
        cellValeur.setBorder(0);
        cellValeur.setPadding(3);
        table.addCell(cellValeur);
    }

    private void ajouterSemestre(Document document, BulletinSemestreDTO semestre,
                                  BulletinEtudiantDTO bulletin) throws DocumentException {
        Paragraph titreSemestre = new Paragraph(semestre.getSemestreNom(),
                PdfStyleHelper.getSubtitleFont());
        titreSemestre.setSpacingBefore(10);
        titreSemestre.setSpacingAfter(5);
        document.add(titreSemestre);

        for (BulletinUeDTO ue : semestre.getUes()) {
            Paragraph titreUe = new Paragraph(
                    ue.getUeCode() + " — " + ue.getUeNom() + " (" + ue.getCredits() + " ECTS)",
                    PdfStyleHelper.getBoldFont());
            titreUe.setSpacingBefore(5);
            document.add(titreUe);

            PdfPTable tableModules = PdfStyleHelper.createStyledTable(7,
                    new float[]{1.2f, 3f, 1.2f, 1.2f, 1.2f, 1f, 1f});
            tableModules.addCell(PdfStyleHelper.createHeaderCell("Code"));
            tableModules.addCell(PdfStyleHelper.createHeaderCell("Module"));
            tableModules.addCell(PdfStyleHelper.createHeaderCell("Moy. CC"));
            tableModules.addCell(PdfStyleHelper.createHeaderCell("Note Exam"));
            tableModules.addCell(PdfStyleHelper.createHeaderCell("Moyenne"));
            tableModules.addCell(PdfStyleHelper.createHeaderCell("Coeff"));
            tableModules.addCell(PdfStyleHelper.createHeaderCell("Crédits"));

            int row = 0;
            for (BulletinModuleDTO module : ue.getModules()) {
                Color bgColor = (row % 2 == 1) ? PdfStyleHelper.getCouleurSecondaire() : Color.WHITE;

                tableModules.addCell(createColoredCell(module.getModuleCode(), bgColor, false));
                tableModules.addCell(createColoredCell(module.getModuleNom(), bgColor, false));
                tableModules.addCell(createColoredCell(formatNote(module.getMoyenneCC()), bgColor, true));
                tableModules.addCell(createColoredCell(formatNote(module.getNoteExamen()), bgColor, true));
                tableModules.addCell(createColoredCell(formatNote(module.getMoyenneModule()), bgColor, true));
                tableModules.addCell(createColoredCell(String.valueOf(module.getCoefficient()), bgColor, true));
                tableModules.addCell(createColoredCell(String.valueOf(module.getCredits()), bgColor, true));
                row++;
            }

            document.add(tableModules);

            PdfPTable resumeUe = new PdfPTable(3);
            resumeUe.setWidthPercentage(100);
            resumeUe.setWidths(new float[]{2, 1, 1});

            PdfPCell cellMoyUe = new PdfPCell(new Phrase(
                    "Moyenne UE : " + formatNote(ue.getMoyenneUE()), PdfStyleHelper.getBoldFont()));
            cellMoyUe.setBorder(0);
            cellMoyUe.setPadding(4);
            resumeUe.addCell(cellMoyUe);

            PdfPCell cellValidee = new PdfPCell(new Phrase(
                    "Validée : " + (ue.isValidee() ? "Oui" : "Non"), PdfStyleHelper.getBoldFont()));
            cellValidee.setBorder(0);
            cellValidee.setPadding(4);
            cellValidee.setHorizontalAlignment(Element.ALIGN_CENTER);
            resumeUe.addCell(cellValidee);

            PdfPCell cellCreditsUe = new PdfPCell(new Phrase(
                    "Crédits acquis : " + (ue.isValidee() ? ue.getCredits() : 0),
                    PdfStyleHelper.getBoldFont()));
            cellCreditsUe.setBorder(0);
            cellCreditsUe.setPadding(4);
            cellCreditsUe.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumeUe.addCell(cellCreditsUe);

            document.add(resumeUe);
        }

        PdfPTable resumeSemestre = new PdfPTable(2);
        resumeSemestre.setWidthPercentage(100);
        resumeSemestre.setWidths(new float[]{1, 1});
        resumeSemestre.setSpacingBefore(5);

        PdfPCell cellMoySem = new PdfPCell(new Phrase(
                "Moyenne semestre : " + formatNote(semestre.getMoyenneSemestre()),
                PdfStyleHelper.getBoldFont()));
        cellMoySem.setBorder(0);
        cellMoySem.setPadding(5);
        cellMoySem.setBackgroundColor(PdfStyleHelper.getCouleurSecondaire());
        resumeSemestre.addCell(cellMoySem);

        PdfPCell cellCreditsSem = new PdfPCell(new Phrase(
                "Crédits validés : " + semestre.getCreditsValidesSemestre(),
                PdfStyleHelper.getBoldFont()));
        cellCreditsSem.setBorder(0);
        cellCreditsSem.setPadding(5);
        cellCreditsSem.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellCreditsSem.setBackgroundColor(PdfStyleHelper.getCouleurSecondaire());
        resumeSemestre.addCell(cellCreditsSem);

        document.add(resumeSemestre);
    }

    private PdfPCell createColoredCell(String text, Color bgColor, boolean centered) {
        Font font = PdfStyleHelper.getNormalFont();
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (centered) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        return cell;
    }

    private void ajouterRecapitulatifFinal(Document document, BulletinEtudiantDTO bulletin)
            throws DocumentException {
        PdfStyleHelper.addEmptyLine(document, 1);

        Paragraph titreRecap = new Paragraph("RÉCAPITULATIF", PdfStyleHelper.getSubtitleFont());
        titreRecap.setSpacingAfter(10);
        document.add(titreRecap);

        Font grandBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, Color.BLACK);

        PdfPTable tableRecap = new PdfPTable(2);
        tableRecap.setWidthPercentage(80);
        tableRecap.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableRecap.setWidths(new float[]{1, 1});

        ajouterLigneRecap(tableRecap, "Moyenne annuelle",
                formatNote(bulletin.getMoyenneAnnuelle()) + " / 20", grandBold);
        ajouterLigneRecap(tableRecap, "Crédits validés",
                bulletin.getCreditsValides() + " / " + bulletin.getCreditsTotaux(),
                PdfStyleHelper.getBoldFont());
        ajouterLigneRecap(tableRecap, "Décision du jury",
                bulletin.getDecision() != null ? bulletin.getDecision() : "EN_ATTENTE",
                PdfStyleHelper.getBoldFont());
        if (bulletin.getMention() != null) {
            ajouterLigneRecap(tableRecap, "Mention", bulletin.getMention(),
                    PdfStyleHelper.getBoldFont());
        }

        document.add(tableRecap);
    }

    private void ajouterLigneRecap(PdfPTable table, String label, String valeur, Font font) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, PdfStyleHelper.getBoldFont()));
        cellLabel.setPadding(8);
        cellLabel.setBackgroundColor(PdfStyleHelper.getCouleurSecondaire());
        table.addCell(cellLabel);

        PdfPCell cellValeur = new PdfPCell(new Phrase(valeur, font));
        cellValeur.setPadding(8);
        cellValeur.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cellValeur);
    }

    private void ajouterZoneSignature(Document document) throws DocumentException {
        PdfStyleHelper.addEmptyLine(document, 2);

        Paragraph lieu = new Paragraph(
                "Fait à Dakar, le " + LocalDate.now().format(DATE_FORMATTER),
                PdfStyleHelper.getNormalFont());
        lieu.setAlignment(Element.ALIGN_RIGHT);
        document.add(lieu);

        PdfStyleHelper.addEmptyLine(document, 3);

        Paragraph signataire = new Paragraph("Le Directeur des Études",
                PdfStyleHelper.getBoldFont());
        signataire.setAlignment(Element.ALIGN_RIGHT);
        document.add(signataire);
    }

    private String formatNote(Double note) {
        if (note == null) return "—";
        return String.format("%.2f", note);
    }

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
