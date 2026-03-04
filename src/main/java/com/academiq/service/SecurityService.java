package com.academiq.service;

import com.academiq.entity.Enseignant;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import com.academiq.exception.BusinessException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.AffectationRepository;
import com.academiq.repository.EnseignantRepository;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final AffectationRepository affectationRepository;
    private final EvaluationRepository evaluationRepository;

    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    public Utilisateur getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        String email = auth.getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Utilisateur non trouvé"));
    }

    /**
     * Vérifie si l'utilisateur courant a le rôle donné
     */
    public boolean hasRole(String role) {
        Utilisateur current = getCurrentUser();
        return current.getRole().name().equals(role);
    }

    /**
     * Vérifie si l'utilisateur courant est le propriétaire de la ressource utilisateur
     */
    public boolean isOwner(Long utilisateurId) {
        Utilisateur current = getCurrentUser();
        return current.getId().equals(utilisateurId);
    }

    /**
     * Vérifie si l'utilisateur courant est l'étudiant donné
     */
    public boolean isEtudiant(Long etudiantId) {
        Utilisateur current = getCurrentUser();
        if (current.getRole() != Role.ETUDIANT) return false;
        return etudiantRepository.findByUtilisateurId(current.getId())
                .map(e -> e.getId().equals(etudiantId))
                .orElse(false);
    }

    /**
     * Vérifie si l'utilisateur courant est l'enseignant donné
     */
    public boolean isEnseignant(Long enseignantId) {
        Utilisateur current = getCurrentUser();
        if (current.getRole() != Role.ENSEIGNANT) return false;
        return enseignantRepository.findByUtilisateurId(current.getId())
                .map(e -> e.getId().equals(enseignantId))
                .orElse(false);
    }

    /**
     * Vérifie si l'enseignant courant est affecté au module donné
     */
    public boolean isAffecteAuModule(Long moduleId) {
        Utilisateur current = getCurrentUser();
        if (current.getRole() != Role.ENSEIGNANT) return false;
        Enseignant enseignant = enseignantRepository.findByUtilisateurId(current.getId())
                .orElse(null);
        if (enseignant == null) return false;
        return affectationRepository.existsByEnseignantIdAndModuleFormationIdAndActifTrue(
                enseignant.getId(), moduleId);
    }

    /**
     * Vérifie si l'utilisateur courant est propriétaire OU admin/super_admin
     */
    public boolean isOwnerOrAdmin(Long utilisateurId) {
        return isOwner(utilisateurId) || hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }

    /**
     * Vérifie si l'utilisateur courant peut gérer l'utilisateur cible.
     * SUPER_ADMIN peut gérer tout le monde sauf lui-même.
     * ADMIN peut gérer tout sauf les SUPER_ADMIN et autres ADMIN.
     */
    public boolean canManageUser(Long targetUserId) {
        Utilisateur current = getCurrentUser();
        Utilisateur target = utilisateurRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", targetUserId));

        if (current.getRole() == Role.SUPER_ADMIN) {
            return !current.getId().equals(targetUserId);
        }

        if (current.getRole() == Role.ADMIN) {
            return target.getRole() != Role.SUPER_ADMIN && target.getRole() != Role.ADMIN;
        }

        return false;
    }

    /**
     * Vérifie qu'au moins un SUPER_ADMIN reste actif dans le système
     */
    public void verifierMinimumSuperAdmin() {
        long count = utilisateurRepository.countByRoleAndActifTrue(Role.SUPER_ADMIN);
        if (count <= 1) {
            throw new BusinessException("Au moins un SUPER_ADMIN actif doit exister dans le système");
        }
    }

    /**
     * Vérifie l'accès aux données d'un étudiant.
     * SA, ADMIN, RESP_PEDA : accès libre.
     * ETUDIANT : uniquement ses propres données.
     * Autres rôles : refusé.
     */
    public void verifierAccesEtudiant(Long etudiantId) {
        Utilisateur current = getCurrentUser();
        Role role = current.getRole();

        if (role == Role.SUPER_ADMIN || role == Role.ADMIN || role == Role.RESPONSABLE_PEDAGOGIQUE) {
            return;
        }

        if (role == Role.ETUDIANT) {
            boolean estProprietaire = etudiantRepository.findByUtilisateurId(current.getId())
                    .map(e -> e.getId().equals(etudiantId))
                    .orElse(false);
            if (estProprietaire) {
                return;
            }
        }

        throw new AccessDeniedException("Accès non autorisé aux données de cet étudiant");
    }

    /**
     * Vérifie l'accès aux données d'un enseignant.
     * SA, ADMIN : accès libre.
     * ENSEIGNANT : uniquement ses propres données.
     * Autres rôles : refusé.
     */
    public void verifierAccesEnseignant(Long enseignantId) {
        Utilisateur current = getCurrentUser();
        Role role = current.getRole();

        if (role == Role.SUPER_ADMIN || role == Role.ADMIN) {
            return;
        }

        if (role == Role.ENSEIGNANT) {
            boolean estProprietaire = enseignantRepository.findByUtilisateurId(current.getId())
                    .map(e -> e.getId().equals(enseignantId))
                    .orElse(false);
            if (estProprietaire) {
                return;
            }
        }

        throw new AccessDeniedException("Accès non autorisé aux données de cet enseignant");
    }

    /**
     * Vérifie l'accès à un module.
     * SA, ADMIN, RESP_PEDA : accès libre.
     * ENSEIGNANT : uniquement s'il est affecté au module.
     * Autres rôles : refusé.
     */
    public void verifierAccesModule(Long moduleId) {
        Utilisateur current = getCurrentUser();
        Role role = current.getRole();

        if (role == Role.SUPER_ADMIN || role == Role.ADMIN || role == Role.RESPONSABLE_PEDAGOGIQUE) {
            return;
        }

        if (role == Role.ENSEIGNANT) {
            Enseignant enseignant = enseignantRepository.findByUtilisateurId(current.getId())
                    .orElse(null);
            if (enseignant != null && affectationRepository
                    .existsByEnseignantIdAndModuleFormationIdAndActifTrue(enseignant.getId(), moduleId)) {
                return;
            }
        }

        throw new AccessDeniedException("Accès non autorisé à ce module");
    }

    /**
     * Vérifie l'accès à une évaluation via le module associé.
     * Délègue à verifierAccesModule après résolution du moduleId.
     */
    public void verifierAccesEvaluation(Long evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation", "id", evaluationId));
        verifierAccesModule(evaluation.getModuleFormation().getId());
    }
}
