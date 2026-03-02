package com.academiq.service;

import com.academiq.dto.note.NoteSaisieDTO;
import com.academiq.dto.note.SaisieEnMasseResult;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Inscription;
import com.academiq.entity.Note;
import com.academiq.entity.StatutInscription;
import com.academiq.exception.BadRequestException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImportNotesService {

    private static final Logger log = LoggerFactory.getLogger(ImportNotesService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final NoteService noteService;
    private final EvaluationRepository evaluationRepository;
    private final EtudiantRepository etudiantRepository;
    private final SaisieEnMasseService saisieEnMasseService;
    private final InscriptionRepository inscriptionRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public SaisieEnMasseResult importerNotesExcel(Long evaluationId, MultipartFile fichier, Long saisiParId) {
        validerFichier(fichier);

        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation", "id", evaluationId));

        List<NoteSaisieDTO> notes = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(fichier.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String matricule = getCellStringValue(row.getCell(0));
                if (matricule == null || matricule.isBlank()) continue;

                Optional<Etudiant> etudiantOpt = etudiantRepository.findByMatricule(matricule.trim());
                if (etudiantOpt.isEmpty()) {
                    log.warn("Matricule non trouvé lors de l'import : {}", matricule);
                    continue;
                }

                Double valeur = getCellNumericValue(row.getCell(3));
                boolean absent = isAbsent(row.getCell(4));
                String commentaire = getCellStringValue(row.getCell(5));

                NoteSaisieDTO dto = NoteSaisieDTO.builder()
                        .etudiantId(etudiantOpt.get().getId())
                        .valeur(valeur)
                        .absent(absent)
                        .commentaire(commentaire)
                        .build();
                notes.add(dto);
            }
        } catch (IOException e) {
            throw new BadRequestException("Erreur lors de la lecture du fichier Excel : " + e.getMessage());
        }

        if (notes.isEmpty()) {
            throw new BadRequestException("Aucune note valide trouvée dans le fichier");
        }

        log.info("Import Excel évaluation {} : {} notes lues", evaluationId, notes.size());
        return saisieEnMasseService.saisirNotesClasse(evaluationId, notes, saisiParId);
    }

    public Workbook genererTemplateExcel(Long evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation", "id", evaluationId));

        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(evaluation.getPromotion().getId(), StatutInscription.ACTIVE);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Notes");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row header = sheet.createRow(0);
        String[] headers = {"Matricule", "Nom", "Prénom",
                "Note (sur " + evaluation.getNoteMaximale() + ")", "Absent (OUI/NON)", "Commentaire"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (Inscription inscription : inscriptions) {
            var etudiant = inscription.getEtudiant();
            var utilisateur = etudiant.getUtilisateur();

            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(etudiant.getMatricule());
            row.createCell(1).setCellValue(utilisateur.getNom());
            row.createCell(2).setCellValue(utilisateur.getPrenom());

            Optional<Note> noteExistante = noteRepository
                    .findByEtudiantIdAndEvaluationId(etudiant.getId(), evaluationId);

            if (noteExistante.isPresent()) {
                Note note = noteExistante.get();
                if (note.getValeur() != null) {
                    row.createCell(3).setCellValue(note.getValeur());
                }
                row.createCell(4).setCellValue(note.isAbsent() ? "OUI" : "NON");
            } else {
                row.createCell(3);
                row.createCell(4).setCellValue("NON");
            }
            row.createCell(5);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    private void validerFichier(MultipartFile fichier) {
        if (fichier.isEmpty()) {
            throw new BadRequestException("Le fichier est vide");
        }
        if (fichier.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Le fichier dépasse la taille maximale de 5 Mo");
        }
        String filename = fichier.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BadRequestException("Format de fichier invalide. Formats acceptés : .xlsx, .xls");
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }

    private Double getCellNumericValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private boolean isAbsent(Cell cell) {
        if (cell == null) return false;
        if (cell.getCellType() == CellType.STRING) {
            return "OUI".equalsIgnoreCase(cell.getStringCellValue().trim());
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        }
        return false;
    }
}
