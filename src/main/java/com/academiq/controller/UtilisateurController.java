package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.PageResponse;
import com.academiq.dto.utilisateur.ImportResult;
import com.academiq.dto.utilisateur.UtilisateurCreateRequest;
import com.academiq.dto.utilisateur.UtilisateurDetailResponse;
import com.academiq.dto.utilisateur.UtilisateurSummaryResponse;
import com.academiq.dto.utilisateur.UtilisateurUpdateRequest;
import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import com.academiq.mapper.UtilisateurProfilMapper;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.security.IsSuperAdmin;
import com.academiq.service.ImportService;
import com.academiq.service.SecurityService;
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
import org.springframework.web.multipart.MultipartFile;

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
    private final ImportService importService;
    private final SecurityService securityService;

    @GetMapping
    @IsAdmin
    public ResponseEntity<ApiResponse<PageResponse<UtilisateurSummaryResponse>>> listerUtilisateurs(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_FIELD) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Utilisateur current = securityService.getCurrentUser();

        Page<Utilisateur> utilisateurs;
        if (role != null) {
            if (current.getRole() == Role.ADMIN && role == Role.SUPER_ADMIN) {
                throw new com.academiq.exception.ForbiddenException("Accès non autorisé à la liste des SUPER_ADMIN");
            }
            utilisateurs = utilisateurService.findByRole(role, PageRequest.of(page, size, sort));
        } else if (current.getRole() == Role.ADMIN) {
            utilisateurs = utilisateurService.findAllExcludingRole(Role.SUPER_ADMIN, PageRequest.of(page, size, sort));
        } else {
            utilisateurs = utilisateurService.findAll(PageRequest.of(page, size, sort));
        }

        PageResponse<UtilisateurSummaryResponse> response = PageResponse.of(
                utilisateurs,
                utilisateurProfilMapper.toSummaryList(utilisateurs.getContent())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<UtilisateurDetailResponse>> getUtilisateur(@PathVariable Long id) {
        Utilisateur current = securityService.getCurrentUser();
        Utilisateur utilisateur = utilisateurService.findById(id);

        if (current.getRole() == Role.ADMIN && utilisateur.getRole() == Role.SUPER_ADMIN) {
            throw new com.academiq.exception.ForbiddenException("Un ADMIN ne peut pas consulter un profil SUPER_ADMIN");
        }
        if (current.getRole() == Role.RESPONSABLE_PEDAGOGIQUE && utilisateur.getRole() != Role.ETUDIANT) {
            throw new com.academiq.exception.ForbiddenException("Un responsable pédagogique ne peut consulter que les profils étudiants");
        }

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
    @IsSuperAdmin
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
    @IsSuperAdmin
    public ResponseEntity<ApiResponse<Void>> changerRole(
            @PathVariable Long id,
            @Valid @RequestBody com.academiq.dto.utilisateur.ChangeRoleRequest request) {
        utilisateurService.changeRole(id, request.getRole(), request.getMotif());
        return ResponseEntity.ok(ApiResponse.success("Rôle modifié avec succès"));
    }

    @GetMapping("/recherche")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<PageResponse<UtilisateurSummaryResponse>>> rechercherUtilisateurs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_FIELD) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Utilisateur current = securityService.getCurrentUser();
        Page<Utilisateur> utilisateurs = utilisateurService.rechercher(keyword, PageRequest.of(page, size, sort));

        java.util.List<Utilisateur> filtered = utilisateurs.getContent().stream()
                .filter(u -> {
                    if (current.getRole() == Role.ADMIN && u.getRole() == Role.SUPER_ADMIN) return false;
                    if (current.getRole() == Role.RESPONSABLE_PEDAGOGIQUE && u.getRole() != Role.ETUDIANT) return false;
                    return true;
                })
                .toList();

        PageResponse<UtilisateurSummaryResponse> response = PageResponse.of(
                utilisateurs,
                utilisateurProfilMapper.toSummaryList(filtered)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    @IsAdmin
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistiques() {
        Utilisateur current = securityService.getCurrentUser();
        Map<String, Object> stats = new HashMap<>();

        long etudiants = utilisateurService.countByRole(Role.ETUDIANT);
        long enseignants = utilisateurService.countByRole(Role.ENSEIGNANT);
        long admins = utilisateurService.countByRole(Role.ADMIN);
        long rp = utilisateurService.countByRole(Role.RESPONSABLE_PEDAGOGIQUE);

        stats.put("etudiants", etudiants);
        stats.put("enseignants", enseignants);
        stats.put("admins", admins);
        stats.put("responsablesPedagogiques", rp);
        stats.put("actifs", utilisateurService.countActifs());

        if (current.getRole() == Role.SUPER_ADMIN) {
            long superAdmins = utilisateurService.countByRole(Role.SUPER_ADMIN);
            stats.put("superAdmins", superAdmins);
            stats.put("totalUtilisateurs", etudiants + enseignants + admins + rp + superAdmins);
        } else {
            stats.put("totalUtilisateurs", etudiants + enseignants + admins + rp);
        }

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/import-etudiants")
    @IsAdmin
    public ResponseEntity<ApiResponse<ImportResult>> importerEtudiants(
            @RequestParam("fichier") MultipartFile fichier) {
        log.info("Import CSV d'étudiants : {}", fichier.getOriginalFilename());
        ImportResult result = importService.importerEtudiantsCSV(fichier);
        String message = String.format("Import terminé : %d importés, %d échecs", result.getImportes(), result.getEchecs());
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }
}
