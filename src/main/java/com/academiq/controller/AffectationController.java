package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.structure.AffectationRequest;
import com.academiq.dto.structure.AffectationResponse;
import com.academiq.entity.Affectation;
import com.academiq.mapper.StructureMapper;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.security.IsEnseignantOrAdmin;
import com.academiq.service.AffectationService;
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
@RequestMapping("/affectations")
@RequiredArgsConstructor
@Tag(name = "Affectations", description = "Gestion des affectations enseignant-module")
public class AffectationController {

    private final AffectationService affectationService;
    private final StructureMapper structureMapper;

    @PostMapping
    @IsAdmin
    public ResponseEntity<ApiResponse<AffectationResponse>> affecterEnseignant(@Valid @RequestBody AffectationRequest request) {
        Affectation affectation = affectationService.affecterEnseignant(
                request.getEnseignantId(), request.getModuleFormationId(),
                request.getPromotionId(), request.getAnneeUniversitaire());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Affectation réussie", structureMapper.toAffectationResponse(affectation)));
    }

    @GetMapping("/enseignant/{enseignantId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<List<AffectationResponse>>> getAffectationsEnseignant(@PathVariable Long enseignantId) {
        List<AffectationResponse> affectations = structureMapper.toAffectationResponseList(
                affectationService.getAffectationsByEnseignant(enseignantId));
        return ResponseEntity.ok(ApiResponse.success(affectations));
    }

    @GetMapping("/module/{moduleId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<AffectationResponse>>> getAffectationsModule(@PathVariable Long moduleId) {
        List<AffectationResponse> affectations = structureMapper.toAffectationResponseList(
                affectationService.getAffectationsByModule(moduleId));
        return ResponseEntity.ok(ApiResponse.success(affectations));
    }

    @PatchMapping("/{id}/desactiver")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> desactiverAffectation(@PathVariable Long id) {
        affectationService.desactiverAffectation(id);
        return ResponseEntity.ok(ApiResponse.success("Affectation désactivée avec succès"));
    }
}
