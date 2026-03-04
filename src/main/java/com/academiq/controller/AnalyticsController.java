package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.stats.ComparaisonPromotionsDTO;
import com.academiq.dto.stats.DashboardAdminDTO;
import com.academiq.dto.stats.DashboardEnseignantDTO;
import com.academiq.dto.stats.DashboardEtudiantDTO;
import com.academiq.dto.stats.DistributionNotesDTO;
import com.academiq.dto.stats.EvolutionModuleDTO;
import com.academiq.dto.stats.EvolutionPerformanceDTO;
import com.academiq.dto.stats.TauxReussiteDTO;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import com.academiq.repository.EtudiantRepository;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.security.IsAuthenticated;
import com.academiq.security.IsEnseignantOrAdmin;
import com.academiq.service.StatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Dashboards", description = "Tableaux de bord et statistiques analytiques")
public class AnalyticsController {

    private final StatsService statsService;
    private final EtudiantRepository etudiantRepository;

    // ======================== Dashboards ========================

    @GetMapping("/dashboard/admin")
    @IsAdmin
    public ResponseEntity<ApiResponse<DashboardAdminDTO>> getDashboardAdmin() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getDashboardAdmin()));
    }

    @GetMapping("/dashboard/enseignant/{enseignantId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<DashboardEnseignantDTO>> getDashboardEnseignant(
            @PathVariable Long enseignantId) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getDashboardEnseignant(enseignantId)));
    }

    @GetMapping("/dashboard/etudiant/{etudiantId}")
    @IsAuthenticated
    public ResponseEntity<ApiResponse<DashboardEtudiantDTO>> getDashboardEtudiant(
            @PathVariable Long etudiantId,
            @AuthenticationPrincipal Utilisateur utilisateur) {
        verifierAccesEtudiant(etudiantId, utilisateur);
        return ResponseEntity.ok(ApiResponse.success(statsService.getDashboardEtudiant(etudiantId)));
    }

    // ======================== Taux de réussite ========================

    @GetMapping("/taux-reussite/module/{moduleId}/promotion/{promotionId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<TauxReussiteDTO>> getTauxReussiteModule(
            @PathVariable Long moduleId, @PathVariable Long promotionId) {
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerTauxReussiteModule(moduleId, promotionId)));
    }

    @GetMapping("/taux-reussite/ue/{ueId}/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<TauxReussiteDTO>> getTauxReussiteUE(
            @PathVariable Long ueId, @PathVariable Long promotionId) {
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerTauxReussiteUE(ueId, promotionId)));
    }

    @GetMapping("/taux-reussite/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<TauxReussiteDTO>> getTauxReussitePromotion(
            @PathVariable Long promotionId) {
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerTauxReussitePromotion(promotionId)));
    }

    // ======================== Distribution ========================

    @GetMapping("/distribution/module/{moduleId}/promotion/{promotionId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<DistributionNotesDTO>> getDistributionModule(
            @PathVariable Long moduleId, @PathVariable Long promotionId) {
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerDistributionModule(moduleId, promotionId)));
    }

    @GetMapping("/distribution/evaluation/{evaluationId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<DistributionNotesDTO>> getDistributionEvaluation(
            @PathVariable Long evaluationId) {
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerDistributionEvaluation(evaluationId)));
    }

    // ======================== Évolution ========================

    @GetMapping("/evolution/etudiant/{etudiantId}")
    @IsAuthenticated
    public ResponseEntity<ApiResponse<EvolutionPerformanceDTO>> getEvolutionEtudiant(
            @PathVariable Long etudiantId,
            @AuthenticationPrincipal Utilisateur utilisateur) {
        verifierAccesEtudiant(etudiantId, utilisateur);
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerEvolutionEtudiant(etudiantId)));
    }

    @GetMapping("/evolution/module/{moduleId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<EvolutionModuleDTO>> getEvolutionModule(
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerEvolutionModule(moduleId)));
    }

    // ======================== Comparaison ========================

    @GetMapping("/comparaison")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<ComparaisonPromotionsDTO>> comparerPromotions(
            @RequestParam List<Long> promotionIds) {
        return ResponseEntity.ok(ApiResponse.success(
                statsService.comparerPromotions(promotionIds)));
    }

    // ======================== Utilitaire ========================

    private void verifierAccesEtudiant(Long etudiantId, Utilisateur utilisateur) {
        if (utilisateur.getRole() == Role.SUPER_ADMIN
                || utilisateur.getRole() == Role.ADMIN
                || utilisateur.getRole() == Role.RESPONSABLE_PEDAGOGIQUE
                || utilisateur.getRole() == Role.ENSEIGNANT) {
            return;
        }

        Etudiant etudiant = etudiantRepository.findByUtilisateurId(utilisateur.getId())
                .orElse(null);

        if (etudiant == null || !etudiant.getId().equals(etudiantId)) {
            throw new AccessDeniedException("Accès non autorisé à ces données");
        }
    }
}
