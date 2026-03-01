package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.structure.InscriptionRequest;
import com.academiq.dto.structure.InscriptionResponse;
import com.academiq.entity.Inscription;
import com.academiq.mapper.StructureMapper;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.service.InscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inscriptions")
@RequiredArgsConstructor
@Tag(name = "Inscriptions", description = "Gestion des inscriptions étudiant-promotion")
public class InscriptionController {

    private final InscriptionService inscriptionService;
    private final StructureMapper structureMapper;

    @PostMapping
    @IsAdmin
    public ResponseEntity<ApiResponse<InscriptionResponse>> inscrireEtudiant(@Valid @RequestBody InscriptionRequest request) {
        Inscription inscription = inscriptionService.inscrireEtudiant(
                request.getEtudiantId(), request.getPromotionId(), request.isRedoublant());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription réussie", structureMapper.toInscriptionResponse(inscription)));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<InscriptionResponse>>> getInscriptionsEtudiant(@PathVariable Long etudiantId) {
        List<InscriptionResponse> inscriptions = structureMapper.toInscriptionResponseList(
                inscriptionService.getInscriptionsByEtudiant(etudiantId));
        return ResponseEntity.ok(ApiResponse.success(inscriptions));
    }

    @GetMapping("/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<InscriptionResponse>>> getInscriptionsPromotion(@PathVariable Long promotionId) {
        List<InscriptionResponse> inscriptions = structureMapper.toInscriptionResponseList(
                inscriptionService.getInscriptionsByPromotion(promotionId));
        return ResponseEntity.ok(ApiResponse.success(inscriptions));
    }

    @PatchMapping("/{id}/annuler")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> annulerInscription(@PathVariable Long id) {
        inscriptionService.annulerInscription(id);
        return ResponseEntity.ok(ApiResponse.success("Inscription annulée avec succès"));
    }
}
