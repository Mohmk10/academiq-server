package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.entity.Promotion;
import com.academiq.repository.PromotionRepository;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsSuperAdmin;
import com.academiq.service.DatabaseResetService;
import com.academiq.service.ExportExcelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Opérations d'administration système")
public class AdminController {

    private final DatabaseResetService databaseResetService;
    private final ExportExcelService exportExcelService;
    private final PromotionRepository promotionRepository;

    @PostMapping("/reset-database")
    @IsSuperAdmin
    public ResponseEntity<ApiResponse<Void>> resetDatabase(@RequestParam("confirm") String confirm) {
        if (!"RESET_ALL".equals(confirm)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Confirmation invalide. Envoyez confirm=RESET_ALL"));
        }

        databaseResetService.resetDatabase();
        return ResponseEntity.ok(ApiResponse.success("Base de données réinitialisée avec succès"));
    }

    @GetMapping("/export-donnees")
    @IsAdmin
    public ResponseEntity<byte[]> exporterToutesDonnees() throws IOException {
        List<Promotion> promotions = promotionRepository.findByActifTrue();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            for (Promotion promotion : promotions) {
                try {
                    byte[] excel = exportExcelService.exporterNotesPromotion(promotion.getId());
                    String fileName = "promotion_" + promotion.getNom().replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx";
                    zip.putNextEntry(new ZipEntry(fileName));
                    zip.write(excel);
                    zip.closeEntry();
                } catch (Exception e) {
                    // Skip promotions without data
                }
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "export_academiq_complet.zip");

        return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }
}
