package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.calcul.BulletinEtudiantDTO;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.security.IsEnseignantOrAdmin;
import com.academiq.service.BulletinService;
import com.academiq.service.CalculService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/calculs")
@RequiredArgsConstructor
@Tag(name = "Calculs & Moyennes", description = "Calculs académiques, moyennes et bulletins")
public class CalculController {

    private final CalculService calculService;
    private final BulletinService bulletinService;

    @GetMapping("/bulletin/etudiant/{etudiantId}/promotion/{promotionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BulletinEtudiantDTO>> getBulletin(
            @PathVariable Long etudiantId, @PathVariable Long promotionId) {
        return ResponseEntity.ok(ApiResponse.success(
                bulletinService.genererBulletin(etudiantId, promotionId)));
    }

    @GetMapping("/moyenne-module/etudiant/{etudiantId}/module/{moduleId}/promotion/{promotionId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMoyenneModule(
            @PathVariable Long etudiantId, @PathVariable Long moduleId, @PathVariable Long promotionId) {
        Double moyenne = calculService.calculerMoyenneModule(etudiantId, moduleId, promotionId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("etudiantId", etudiantId, "moduleId", moduleId, "moyenne", moyenne != null ? moyenne : "N/A")));
    }

    @GetMapping("/moyenne-ue/etudiant/{etudiantId}/ue/{ueId}/promotion/{promotionId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMoyenneUE(
            @PathVariable Long etudiantId, @PathVariable Long ueId, @PathVariable Long promotionId) {
        Double moyenne = calculService.calculerMoyenneUE(etudiantId, ueId, promotionId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("etudiantId", etudiantId, "ueId", ueId, "moyenne", moyenne != null ? moyenne : "N/A")));
    }

    @GetMapping("/moyenne-semestre/etudiant/{etudiantId}/semestre/{semestreId}/promotion/{promotionId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMoyenneSemestre(
            @PathVariable Long etudiantId, @PathVariable Long semestreId, @PathVariable Long promotionId) {
        Double moyenne = calculService.calculerMoyenneSemestre(etudiantId, semestreId, promotionId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("etudiantId", etudiantId, "semestreId", semestreId, "moyenne", moyenne != null ? moyenne : "N/A")));
    }

    @GetMapping("/moyenne-annuelle/etudiant/{etudiantId}/niveau/{niveauId}/promotion/{promotionId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMoyenneAnnuelle(
            @PathVariable Long etudiantId, @PathVariable Long niveauId, @PathVariable Long promotionId) {
        Double moyenne = calculService.calculerMoyenneAnnuelle(etudiantId, niveauId, promotionId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("etudiantId", etudiantId, "niveauId", niveauId, "moyenne", moyenne != null ? moyenne : "N/A")));
    }

    @GetMapping("/credits/etudiant/{etudiantId}/niveau/{niveauId}/promotion/{promotionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCredits(
            @PathVariable Long etudiantId, @PathVariable Long niveauId, @PathVariable Long promotionId) {
        int creditsValides = calculService.calculerCreditsAnnuels(etudiantId, niveauId, promotionId);
        int creditsRequis = calculService.calculerCreditsTotauxRequis(niveauId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("etudiantId", etudiantId, "creditsValides", creditsValides, "creditsRequis", creditsRequis)));
    }

    @GetMapping("/decision/etudiant/{etudiantId}/niveau/{niveauId}/promotion/{promotionId}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDecision(
            @PathVariable Long etudiantId, @PathVariable Long niveauId, @PathVariable Long promotionId) {
        var decision = calculService.determinerDecision(etudiantId, niveauId, promotionId);
        Double moyenne = calculService.calculerMoyenneAnnuelle(etudiantId, niveauId, promotionId);
        var mention = calculService.determinerMention(moyenne);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("etudiantId", etudiantId,
                        "decision", decision.name(),
                        "mention", mention != null ? mention.name() : "AUCUNE",
                        "moyenne", moyenne != null ? moyenne : "N/A")));
    }
}
