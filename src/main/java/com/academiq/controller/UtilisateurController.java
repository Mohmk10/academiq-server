package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.PageResponse;
import com.academiq.dto.utilisateur.UtilisateurCreateRequest;
import com.academiq.dto.utilisateur.UtilisateurDetailResponse;
import com.academiq.dto.utilisateur.UtilisateurSummaryResponse;
import com.academiq.dto.utilisateur.UtilisateurUpdateRequest;
import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import com.academiq.mapper.UtilisateurProfilMapper;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.service.UtilisateurService;
import com.academiq.util.PaginationConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs")
public class UtilisateurController {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurController.class);

    private final UtilisateurService utilisateurService;
    private final UtilisateurProfilMapper utilisateurProfilMapper;

    @GetMapping
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<PageResponse<UtilisateurSummaryResponse>>> listerUtilisateurs(
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_FIELD) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Utilisateur> utilisateurs = utilisateurService.findAll(PageRequest.of(page, size, sort));
        PageResponse<UtilisateurSummaryResponse> response = PageResponse.of(
                utilisateurs,
                utilisateurProfilMapper.toSummaryList(utilisateurs.getContent())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<UtilisateurDetailResponse>> getUtilisateur(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.findById(id);
        UtilisateurDetailResponse response = utilisateurProfilMapper.toDetailResponse(utilisateur);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @IsAdmin
    public ResponseEntity<ApiResponse<UtilisateurDetailResponse>> creerUtilisateur(
            @Valid @RequestBody UtilisateurCreateRequest request) {
        log.info("Création d'un utilisateur : {} {} ({})", request.getPrenom(), request.getNom(), request.getRole());
        Utilisateur utilisateur = utilisateurService.createUtilisateur(request);
        UtilisateurDetailResponse response = utilisateurProfilMapper.toDetailResponse(utilisateur);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Utilisateur créé avec succès", response));
    }

    @PutMapping("/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<UtilisateurDetailResponse>> modifierUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateRequest request) {
        log.info("Modification de l'utilisateur : {}", id);
        Utilisateur utilisateur = utilisateurService.updateUtilisateurComplet(id, request);
        UtilisateurDetailResponse response = utilisateurProfilMapper.toDetailResponse(utilisateur);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur modifié avec succès", response));
    }

    @DeleteMapping("/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> supprimerUtilisateur(@PathVariable Long id) {
        log.info("Suppression de l'utilisateur : {}", id);
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé avec succès"));
    }

    @PatchMapping("/{id}/toggle-activation")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> toggleActivation(@PathVariable Long id) {
        utilisateurService.toggleActivation(id);
        return ResponseEntity.ok(ApiResponse.success("Statut d'activation modifié"));
    }

    @PatchMapping("/{id}/role")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> changerRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        utilisateurService.changeRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Rôle modifié avec succès"));
    }

    @GetMapping("/stats")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUtilisateurs", utilisateurService.countByRole(Role.ETUDIANT)
                + utilisateurService.countByRole(Role.ENSEIGNANT)
                + utilisateurService.countByRole(Role.ADMIN)
                + utilisateurService.countByRole(Role.RESPONSABLE_PEDAGOGIQUE));
        stats.put("etudiants", utilisateurService.countByRole(Role.ETUDIANT));
        stats.put("enseignants", utilisateurService.countByRole(Role.ENSEIGNANT));
        stats.put("admins", utilisateurService.countByRole(Role.ADMIN));
        stats.put("responsablesPedagogiques", utilisateurService.countByRole(Role.RESPONSABLE_PEDAGOGIQUE));
        stats.put("actifs", utilisateurService.countActifs());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
