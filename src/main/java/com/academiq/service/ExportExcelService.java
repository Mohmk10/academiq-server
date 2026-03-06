package com.academiq.service;

import com.academiq.dto.calcul.BulletinEtudiantDTO;
import com.academiq.entity.DecisionJury;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Inscription;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Note;
import com.academiq.entity.StatutInscription;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.NoteRepository;
import com.academiq.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportExcelService {

    private static final Logger log = LoggerFactory.getLogger(ExportExcelService.class);

    private final BulletinService bulletinService;
    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PromotionRepository promotionRepository;
    private final EvaluationRepository evaluationRepository;
    private final NoteRepository noteRepository;
    private final ModuleFormationRepository moduleFormationRepository;
    private final CalculService calculService;

    public byte[] exporterNotesPromotion(Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        List<BulletinEtudiantDTO> bulletins = new ArrayList<>();
        for (Inscription inscription : inscriptions) {
            try {
                BulletinEtudiantDTO bulletin = bulletinService.genererBulletin(
                        inscription.getEtudiant().getId(), promotionId);
                bulletins.add(bulletin);
            } catch (Exception e) {
                log.warn("Impossible de générer le bulletin pour l'étudiant {}",
                        inscription.getEtudiant().getId(), e);
            }
        }

        bulletins.sort(Comparator.comparing(
                BulletinEtudiantDTO::getMoyenneAnnuelle,
                Comparator.nullsLast(Comparator.reverseOrder())));

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            Sheet resultats = workbook.createSheet("Résultats");
            String[] headers = {"Matricule", "Nom", "Prénom", "Moy. S1", "Moy. S2",
                    "Moy. Annuelle", "Crédits", "Décision", "Mention"};

            Row headerRow = resultats.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int nbAdmis = 0;
            int nbAjournes = 0;
            int nbRattrapage = 0;
            double sommeMoyennes = 0;
            int nbMoyennes = 0;

            int rowIndex = 1;
            for (BulletinEtudiantDTO b : bulletins) {
                Row row = resultats.createRow(rowIndex++);

                createDataCell(row, 0, b.getEtudiantMatricule(), dataStyle);
                createDataCell(row, 1, b.getEtudiantNom(), dataStyle);
                createDataCell(row, 2, b.getEtudiantPrenom(), dataStyle);

                Double moyS1 = (b.getSemestres() != null && !b.getSemestres().isEmpty())
                        ? b.getSemestres().get(0).getMoyenneSemestre() : null;
                Double moyS2 = (b.getSemestres() != null && b.getSemestres().size() > 1)
                        ? b.getSemestres().get(1).getMoyenneSemestre() : null;

                createNumericCell(row, 3, moyS1, dataStyle);
                createNumericCell(row, 4, moyS2, dataStyle);
                createNumericCell(row, 5, b.getMoyenneAnnuelle(), dataStyle);
                createDataCell(row, 6, b.getCreditsValides() + "/" + b.getCreditsTotaux(), dataStyle);
                createDataCell(row, 7, b.getDecision() != null ? b.getDecision() : "—", dataStyle);
                createDataCell(row, 8, b.getMention() != null ? b.getMention() : "—", dataStyle);

                if (DecisionJury.ADMIS.name().equals(b.getDecision())
                        || DecisionJury.ADMIS_COMPENSATION.name().equals(b.getDecision())) {
                    nbAdmis++;
                } else if (DecisionJury.AJOURNE.name().equals(b.getDecision())) {
                    nbAjournes++;
                } else if (DecisionJury.RATTRAPAGE.name().equals(b.getDecision())) {
                    nbRattrapage++;
                }

                if (b.getMoyenneAnnuelle() != null) {
                    sommeMoyennes += b.getMoyenneAnnuelle();
                    nbMoyennes++;
                }
            }

            for (int i = 0; i < headers.length; i++) {
                resultats.autoSizeColumn(i);
            }

            Sheet stats = workbook.createSheet("Statistiques");
            int totalInscrits = bulletins.size();
            double tauxReussite = totalInscrits > 0 ? (nbAdmis * 100.0 / totalInscrits) : 0;
            double moyennePromo = nbMoyennes > 0 ? sommeMoyennes / nbMoyennes : 0;

            Row statsHeader = stats.createRow(0);
            Cell statsHeaderCell = statsHeader.createCell(0);
            statsHeaderCell.setCellValue("Statistique");
            statsHeaderCell.setCellStyle(headerStyle);
            Cell statsHeaderVal = statsHeader.createCell(1);
            statsHeaderVal.setCellValue("Valeur");
            statsHeaderVal.setCellStyle(headerStyle);

            ajouterLigneStats(stats, 1, "Total inscrits", String.valueOf(totalInscrits), dataStyle);
            ajouterLigneStats(stats, 2, "Admis", String.valueOf(nbAdmis), dataStyle);
            ajouterLigneStats(stats, 3, "Ajournés", String.valueOf(nbAjournes), dataStyle);
            ajouterLigneStats(stats, 4, "Rattrapage", String.valueOf(nbRattrapage), dataStyle);
            ajouterLigneStats(stats, 5, "Taux de réussite", String.format("%.1f%%", tauxReussite), dataStyle);
            ajouterLigneStats(stats, 6, "Moyenne promotion", String.format("%.2f", moyennePromo), dataStyle);

            stats.autoSizeColumn(0);
            stats.autoSizeColumn(1);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            log.info("Export Excel généré pour la promotion {}", promotionId);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Erreur lors de la génération de l'export Excel", e);
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    public byte[] exporterNotesModule(Long moduleId, Long promotionId) {
        ModuleFormation module = moduleFormationRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        List<Evaluation> evaluations = evaluationRepository
                .findByModuleFormationIdAndPromotionId(moduleId, promotionId);

        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            Sheet sheet = workbook.createSheet("Notes " + module.getCode());

            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            createStyledCell(headerRow, colIndex++, "Matricule", headerStyle);
            createStyledCell(headerRow, colIndex++, "Nom", headerStyle);
            createStyledCell(headerRow, colIndex++, "Prénom", headerStyle);

            for (Evaluation eval : evaluations) {
                createStyledCell(headerRow, colIndex++, eval.getNom(), headerStyle);
            }
            createStyledCell(headerRow, colIndex, "Moyenne Module", headerStyle);

            int rowIndex = 1;
            for (Inscription inscription : inscriptions) {
                Row row = sheet.createRow(rowIndex++);
                var etudiant = inscription.getEtudiant();
                var utilisateur = etudiant.getUtilisateur();

                int col = 0;
                createDataCell(row, col++, etudiant.getMatricule(), dataStyle);
                createDataCell(row, col++, utilisateur.getNom(), dataStyle);
                createDataCell(row, col++, utilisateur.getPrenom(), dataStyle);

                for (Evaluation eval : evaluations) {
                    Note note = noteRepository
                            .findByEtudiantIdAndEvaluationId(etudiant.getId(), eval.getId())
                            .orElse(null);

                    if (note != null && note.isAbsent()) {
                        createDataCell(row, col++, "ABS", dataStyle);
                    } else if (note != null && note.getValeur() != null) {
                        createNumericCell(row, col++, note.getValeur(), dataStyle);
                    } else {
                        createDataCell(row, col++, "—", dataStyle);
                    }
                }

                Double moyenne = calculService.calculerMoyenneModule(
                        etudiant.getId(), moduleId, promotionId);
                createNumericCell(row, col, moyenne, dataStyle);
            }

            int totalCols = 3 + evaluations.size() + 1;
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            log.info("Export Excel module {} généré pour la promotion {}", moduleId, promotionId);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Erreur lors de la génération de l'export Excel module", e);
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createDataCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "—");
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, Double value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(Math.round(value * 100.0) / 100.0);
        } else {
            cell.setCellValue("—");
        }
        cell.setCellStyle(style);
    }

    private void createStyledCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void ajouterLigneStats(Sheet sheet, int rowIndex, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        createDataCell(row, 0, label, style);
        createDataCell(row, 1, value, style);
    }
}
