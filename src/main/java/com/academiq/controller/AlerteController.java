package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.alerte.AlerteResponse;
import com.academiq.dto.alerte.RegleAlerteRequest;
import com.academiq.dto.alerte.RegleAlerteResponse;
import com.academiq.dto.alerte.StatistiquesAlertesDTO;
import com.academiq.dto.alerte.TraiterAlerteRequest;
import com.academiq.entity.Alerte;
import com.academiq.entity.NiveauAlerte;
import com.academiq.entity.StatutAlerte;
import com.academiq.entity.TypeAlerte;
import com.academiq.entity.Utilisateur;
import com.academiq.mapper.AlerteMapper;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.security.IsEnseignantOrAdmin;
import com.academiq.service.AlerteService;
import com.academiq.service.DetectionAlerteService;
import com.academiq.service.RegleAlerteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alertes")
@RequiredArgsConstructor
@Tag(name = "Alertes Pédagogiques", description = "Gestion des alertes et règles d'alerte")
public class AlerteController {

    private final AlerteService alerteService;
    private final DetectionAlerteService detectionAlerteService;
    private final RegleAlerteService regleAlerteService;
    private final AlerteMapper alerteMapper;

    // ======================== Alertes ========================

    @GetMapping("/{id}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<AlerteResponse>> getAlerte(@PathVariable Long id) {
        Alerte alerte = alerteService.getAlerteById(id);
        return ResponseEntity.ok(ApiResponse.success(alerteMapper.toAlerteResponse(alerte)));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AlerteResponse>>> getAlertesByEtudiant(
            @PathVariable Long etudiantId) {
        List<Alerte> alertes = alerteService.getAlertesByEtudiant(etudiantId);
        return ResponseEntity.ok(ApiResponse.success(alerteMapper.toAlerteResponseList(alertes)));
    }

    @GetMapping("/actives")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<AlerteResponse>>> getAlertesActives() {
        List<Alerte> alertes = alerteService.getAlertesActives();
        return ResponseEntity.ok(ApiResponse.success(alerteMapper.toAlerteResponseList(alertes)));
    }

    @GetMapping("/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<AlerteResponse>>> getAlertesByPromotion(
            @PathVariable Long promotionId) {
        List<Alerte> alertes = alerteService.getAlertesByPromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.success(alerteMapper.toAlerteResponseList(alertes)));
    }

    @GetMapping("/rechercher")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<Page<AlerteResponse>>> rechercherAlertes(
            @RequestParam(required = false) StatutAlerte statut,
            @RequestParam(required = false) NiveauAlerte niveau,
            @RequestParam(required = false) TypeAlerte type,
            Pageable pageable) {
        Page<Alerte> page = alerteService.rechercherAlertes(statut, niveau, type, pageable);
        Page<AlerteResponse> responsePage = page.map(alerteMapper::toAlerteResponse);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/statistiques")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<StatistiquesAlertesDTO>> getStatistiques() {
        StatistiquesAlertesDTO stats = alerteMapper.toStatistiquesDTO(
                alerteService.getStatistiquesAlertes());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ======================== Actions sur alertes ========================

    @PatchMapping("/{id}/traiter")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<AlerteResponse>> traiterAlerte(
            @PathVariable Long id,
            @Valid @RequestBody TraiterAlerteRequest request,
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Alerte alerte = alerteService.traiterAlerte(id, utilisateur.getId(), request.getCommentaire());
        return ResponseEntity.ok(ApiResponse.success("Alerte traitée avec succès",
                alerteMapper.toAlerteResponse(alerte)));
    }

    @PatchMapping("/{id}/resoudre")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<AlerteResponse>> resoudreAlerte(@PathVariable Long id) {
        Alerte alerte = alerteService.resoudreAlerte(id);
        return ResponseEntity.ok(ApiResponse.success("Alerte résolue avec succès",
                alerteMapper.toAlerteResponse(alerte)));
    }

    @PatchMapping("/{id}/ignorer")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<AlerteResponse>> ignorerAlerte(
            @PathVariable Long id,
            @Valid @RequestBody TraiterAlerteRequest request) {
        Alerte alerte = alerteService.ignorerAlerte(id, request.getCommentaire());
        return ResponseEntity.ok(ApiResponse.success("Alerte ignorée",
                alerteMapper.toAlerteResponse(alerte)));
    }

    // ======================== Détection ========================

    @PostMapping("/analyser/etudiant/{etudiantId}/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<AlerteResponse>>> analyserEtudiant(
            @PathVariable Long etudiantId, @PathVariable Long promotionId) {
        List<Alerte> alertes = detectionAlerteService.analyserEtudiant(etudiantId, promotionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        String.format("%d alerte(s) détectée(s)", alertes.size()),
                        alerteMapper.toAlerteResponseList(alertes)));
    }

    @PostMapping("/analyser/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<AlerteResponse>>> analyserPromotion(
            @PathVariable Long promotionId) {
        List<Alerte> alertes = detectionAlerteService.analyserPromotion(promotionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        String.format("%d alerte(s) détectée(s) pour la promotion", alertes.size()),
                        alerteMapper.toAlerteResponseList(alertes)));
    }

    // ======================== Règles d'alerte ========================

    @GetMapping("/regles")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<RegleAlerteResponse>>> getAllRegles() {
        return ResponseEntity.ok(ApiResponse.success(
                alerteMapper.toRegleAlerteResponseList(regleAlerteService.getAllRegles())));
    }

    @GetMapping("/regles/actives")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<List<RegleAlerteResponse>>> getReglesActives() {
        return ResponseEntity.ok(ApiResponse.success(
                alerteMapper.toRegleAlerteResponseList(regleAlerteService.getReglesActives())));
    }

    @GetMapping("/regles/{id}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<RegleAlerteResponse>> getRegle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                alerteMapper.toRegleAlerteResponse(regleAlerteService.getRegleById(id))));
    }

    @PostMapping("/regles")
    @IsAdmin
    public ResponseEntity<ApiResponse<RegleAlerteResponse>> creerRegle(
            @Valid @RequestBody RegleAlerteRequest request) {
        var regle = regleAlerteService.creerRegle(alerteMapper.toRegleAlerte(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Règle créée avec succès",
                        alerteMapper.toRegleAlerteResponse(regle)));
    }

    @PutMapping("/regles/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<RegleAlerteResponse>> modifierRegle(
            @PathVariable Long id, @Valid @RequestBody RegleAlerteRequest request) {
        var regle = regleAlerteService.modifierRegle(id, alerteMapper.toRegleAlerte(request));
        return ResponseEntity.ok(ApiResponse.success("Règle modifiée avec succès",
                alerteMapper.toRegleAlerteResponse(regle)));
    }

    @PatchMapping("/regles/{id}/toggle")
    @IsAdmin
    public ResponseEntity<ApiResponse<RegleAlerteResponse>> toggleRegle(@PathVariable Long id) {
        var regle = regleAlerteService.toggleRegle(id);
        String message = regle.isActif() ? "Règle activée" : "Règle désactivée";
        return ResponseEntity.ok(ApiResponse.success(message,
                alerteMapper.toRegleAlerteResponse(regle)));
    }
}
