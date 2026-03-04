package com.academiq.controller;

import com.academiq.entity.Etudiant;
import com.academiq.entity.Promotion;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.PromotionRepository;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.security.IsAllExceptEtudiant;
import com.academiq.service.ExportExcelService;
import com.academiq.service.SecurityService;
import com.academiq.service.pdf.PdfService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.academiq.security.IsAuthenticated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rapports")
@RequiredArgsConstructor
@Tag(name = "Rapports & Documents", description = "Génération et téléchargement de rapports PDF et Excel")
public class RapportController {

    private final PdfService pdfService;
    private final ExportExcelService exportExcelService;
    private final EtudiantRepository etudiantRepository;
    private final PromotionRepository promotionRepository;
    private final ModuleFormationRepository moduleFormationRepository;
    private final SecurityService securityService;

    @GetMapping("/releve/etudiant/{etudiantId}/promotion/{promotionId}")
    @IsAuthenticated
    public ResponseEntity<byte[]> telechargerReleve(
            @PathVariable Long etudiantId,
            @PathVariable Long promotionId) {

        securityService.verifierAccesEtudiant(etudiantId);

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        byte[] pdf = pdfService.genererReleve(etudiantId, promotionId);
        String filename = String.format("releve_%s_%s.pdf",
                etudiant.getMatricule(), promotion.getAnneeUniversitaire());

        return buildPdfResponse(pdf, filename);
    }

    @GetMapping("/attestation/etudiant/{etudiantId}/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<byte[]> telechargerAttestation(
            @PathVariable Long etudiantId,
            @PathVariable Long promotionId) {

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        byte[] pdf = pdfService.genererAttestation(etudiantId, promotionId);
        String filename = String.format("attestation_%s_%s.pdf",
                etudiant.getMatricule(), promotion.getAnneeUniversitaire());

        return buildPdfResponse(pdf, filename);
    }

    @GetMapping("/pv-deliberation/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<byte[]> telechargerPVDeliberation(@PathVariable Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        byte[] pdf = pdfService.genererPVDeliberation(promotionId);
        String filename = String.format("pv_deliberation_%s_%s.pdf",
                promotion.getNom().replaceAll("\\s+", "_"),
                promotion.getAnneeUniversitaire());

        return buildPdfResponse(pdf, filename);
    }

    @GetMapping("/export-excel/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<byte[]> exporterExcelPromotion(@PathVariable Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        byte[] excel = exportExcelService.exporterNotesPromotion(promotionId);
        String filename = String.format("resultats_%s_%s.xlsx",
                promotion.getNom().replaceAll("\\s+", "_"),
                promotion.getAnneeUniversitaire());

        return buildExcelResponse(excel, filename);
    }

    @GetMapping("/export-excel/module/{moduleId}/promotion/{promotionId}")
    @IsAllExceptEtudiant
    public ResponseEntity<byte[]> exporterExcelModule(
            @PathVariable Long moduleId,
            @PathVariable Long promotionId) {

        securityService.verifierAccesModule(moduleId);
        var module = moduleFormationRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        byte[] excel = exportExcelService.exporterNotesModule(moduleId, promotionId);
        String filename = String.format("notes_%s_%s.xlsx",
                module.getCode(),
                promotion.getAnneeUniversitaire());

        return buildExcelResponse(excel, filename);
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] excel, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }
}
