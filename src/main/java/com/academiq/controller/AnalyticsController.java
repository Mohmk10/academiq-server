package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.stats.AuditLogResponse;
import com.academiq.dto.stats.ComparaisonPromotionsDTO;
import com.academiq.dto.stats.DashboardAdminDTO;
import com.academiq.dto.stats.DashboardEnseignantDTO;
import com.academiq.dto.stats.DashboardEtudiantDTO;
import com.academiq.dto.stats.DistributionNotesDTO;
import com.academiq.dto.stats.EvolutionModuleDTO;
import com.academiq.dto.stats.EvolutionPerformanceDTO;
import com.academiq.dto.stats.SystemStatsDTO;
import com.academiq.dto.stats.TauxReussiteDTO;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.security.IsAllExceptEtudiant;
import com.academiq.security.IsAuthenticated;
import com.academiq.security.IsEnseignantOrAdmin;
import com.academiq.security.IsSuperAdmin;
import com.academiq.service.AuditLogService;
import com.academiq.service.SecurityService;
import com.academiq.service.StatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Dashboards", description = "Tableaux de bord et statistiques analytiques")
public class AnalyticsController {

    private final StatsService statsService;
    private final SecurityService securityService;
    private final AuditLogService auditLogService;

    // ======================== Dashboards ========================

    @GetMapping("/dashboard/admin")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<DashboardAdminDTO>> getDashboardAdmin() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getDashboardAdmin()));
    }

    @GetMapping("/dashboard/enseignant/{enseignantId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<DashboardEnseignantDTO>> getDashboardEnseignant(
            @PathVariable Long enseignantId) {
        securityService.verifierAccesEnseignant(enseignantId);
        return ResponseEntity.ok(ApiResponse.success(statsService.getDashboardEnseignant(enseignantId)));
    }

    @GetMapping("/dashboard/etudiant/{etudiantId}")
    @IsAuthenticated
    public ResponseEntity<ApiResponse<DashboardEtudiantDTO>> getDashboardEtudiant(
            @PathVariable Long etudiantId) {
        securityService.verifierAccesEtudiant(etudiantId);
        return ResponseEntity.ok(ApiResponse.success(statsService.getDashboardEtudiant(etudiantId)));
    }

    // ======================== Taux de réussite ========================

    @GetMapping("/taux-reussite/module/{moduleId}/promotion/{promotionId}")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<TauxReussiteDTO>> getTauxReussiteModule(
            @PathVariable Long moduleId, @PathVariable Long promotionId) {
        securityService.verifierAccesModule(moduleId);
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
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<DistributionNotesDTO>> getDistributionModule(
            @PathVariable Long moduleId, @PathVariable Long promotionId) {
        securityService.verifierAccesModule(moduleId);
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerDistributionModule(moduleId, promotionId)));
    }

    @GetMapping("/distribution/evaluation/{evaluationId}")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<DistributionNotesDTO>> getDistributionEvaluation(
            @PathVariable Long evaluationId) {
        securityService.verifierAccesEvaluation(evaluationId);
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerDistributionEvaluation(evaluationId)));
    }

    // ======================== Évolution ========================

    @GetMapping("/evolution/etudiant/{etudiantId}")
    @IsAuthenticated
    public ResponseEntity<ApiResponse<EvolutionPerformanceDTO>> getEvolutionEtudiant(
            @PathVariable Long etudiantId) {
        securityService.verifierAccesEtudiant(etudiantId);
        return ResponseEntity.ok(ApiResponse.success(
                statsService.calculerEvolutionEtudiant(etudiantId)));
    }

    @GetMapping("/evolution/module/{moduleId}")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<EvolutionModuleDTO>> getEvolutionModule(
            @PathVariable Long moduleId) {
        securityService.verifierAccesModule(moduleId);
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

    // ======================== SUPER_ADMIN ========================

    @GetMapping("/audit-logs/recent")
    @IsSuperAdmin
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getRecentAuditLogs(
            @RequestParam(defaultValue = "20") int limit) {
        List<AuditLogResponse> logs = auditLogService.getRecentLogs(limit).stream()
                .map(log -> AuditLogResponse.builder()
                        .id(log.getId())
                        .action(log.getAction().name())
                        .performedBy(log.getPerformedBy())
                        .targetUser(log.getTargetUser())
                        .details(log.getDetails())
                        .date(log.getDate())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/system-stats")
    @IsSuperAdmin
    public ResponseEntity<ApiResponse<SystemStatsDTO>> getSystemStats() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getSystemStats()));
    }
}
