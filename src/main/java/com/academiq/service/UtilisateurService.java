package com.academiq.service;

import com.academiq.dto.utilisateur.UtilisateurCreateRequest;
import com.academiq.dto.utilisateur.UtilisateurUpdateRequest;
import com.academiq.entity.Admin;
import com.academiq.entity.Enseignant;
import com.academiq.entity.Etudiant;
import com.academiq.entity.NiveauAdmin;
import com.academiq.entity.Role;
import com.academiq.entity.StatutEnseignant;
import com.academiq.entity.StatutEtudiant;
import com.academiq.entity.Utilisateur;
import com.academiq.exception.BusinessException;
import com.academiq.exception.DuplicateResourceException;
import com.academiq.exception.ForbiddenException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.AdminRepository;
import com.academiq.repository.EnseignantRepository;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilisateurService {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurService.class);

    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;
    private final AuditLogService auditLogService;

    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));
    }

    public Page<Utilisateur> findAll(Pageable pageable) {
        return utilisateurRepository.findAll(pageable);
    }

    public Page<Utilisateur> findAllExcludingRole(Role role, Pageable pageable) {
        return utilisateurRepository.findByRoleNot(role, pageable);
    }

    public Page<Utilisateur> findByRole(Role role, Pageable pageable) {
        return utilisateurRepository.findByRole(role, pageable);
    }

    @Transactional
    public Utilisateur createUtilisateur(UtilisateurCreateRequest request) {
        if (request.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenException("Le rôle SUPER_ADMIN ne peut pas être attribué via l'API");
        }

        Utilisateur current = securityService.getCurrentUser();
        if (current.getRole() == Role.ADMIN && request.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Un ADMIN ne peut pas créer un autre ADMIN");
        }

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(request.getRole())
                .actif(true)
                .telephone(request.getTelephone())
                .dateNaissance(request.getDateNaissance())
                .adresse(request.getAdresse())
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        switch (request.getRole()) {
            case ETUDIANT -> createEtudiantProfil(utilisateur, request);
            case ENSEIGNANT -> createEnseignantProfil(utilisateur, request);
            case ADMIN -> createAdminProfil(utilisateur, request);
            default -> { }
        }

        log.info("Utilisateur créé : {} {} ({})", utilisateur.getPrenom(), utilisateur.getNom(), utilisateur.getRole());
        return utilisateur;
    }

    @Transactional
    public Utilisateur updateUtilisateurComplet(Long id, UtilisateurUpdateRequest request) {
        Utilisateur current = securityService.getCurrentUser();
        Utilisateur utilisateur = findById(id);

        if (current.getRole() == Role.ADMIN &&
                (utilisateur.getRole() == Role.SUPER_ADMIN || utilisateur.getRole() == Role.ADMIN)) {
            if (!current.getId().equals(id)) {
                throw new ForbiddenException("Un ADMIN ne peut pas modifier un SUPER_ADMIN ou un autre ADMIN");
            }
        }

        if (request.getNom() != null) {
            utilisateur.setNom(request.getNom());
        }
        if (request.getPrenom() != null) {
            utilisateur.setPrenom(request.getPrenom());
        }
        if (request.getTelephone() != null) {
            utilisateur.setTelephone(request.getTelephone());
        }
        if (request.getDateNaissance() != null) {
            utilisateur.setDateNaissance(request.getDateNaissance());
        }
        if (request.getAdresse() != null) {
            utilisateur.setAdresse(request.getAdresse());
        }
        if (request.getPhotoProfil() != null) {
            utilisateur.setPhotoProfil(request.getPhotoProfil());
        }

        utilisateur = utilisateurRepository.save(utilisateur);

        switch (utilisateur.getRole()) {
            case ETUDIANT -> updateEtudiantProfil(utilisateur, request);
            case ENSEIGNANT -> updateEnseignantProfil(utilisateur, request);
            case ADMIN, SUPER_ADMIN -> updateAdminProfil(utilisateur, request);
            default -> { }
        }

        log.info("Utilisateur mis à jour : {}", id);
        return utilisateur;
    }

    @Transactional
    public void deleteUtilisateur(Long id) {
        Utilisateur current = securityService.getCurrentUser();
        Utilisateur utilisateur = findById(id);

        if (current.getId().equals(id)) {
            throw new BusinessException("Impossible de supprimer son propre compte");
        }

        if (utilisateur.getRole() == Role.SUPER_ADMIN) {
            if (current.getRole() != Role.SUPER_ADMIN) {
                throw new ForbiddenException("Seul un SUPER_ADMIN peut supprimer un autre SUPER_ADMIN");
            }
            securityService.verifierMinimumSuperAdmin();
        }

        if (utilisateur.getRole() == Role.ADMIN && current.getRole() != Role.SUPER_ADMIN) {
            throw new ForbiddenException("Seul un SUPER_ADMIN peut supprimer un ADMIN");
        }

        auditLogService.logAccountDeletion(current.getEmail(), utilisateur.getEmail());

        switch (utilisateur.getRole()) {
            case ETUDIANT -> etudiantRepository.findByUtilisateurId(id)
                    .ifPresent(etudiantRepository::delete);
            case ENSEIGNANT -> enseignantRepository.findByUtilisateurId(id)
                    .ifPresent(enseignantRepository::delete);
            case ADMIN, SUPER_ADMIN -> adminRepository.findByUtilisateurId(id)
                    .ifPresent(adminRepository::delete);
            default -> { }
        }

        utilisateurRepository.delete(utilisateur);
        log.info("Utilisateur supprimé : {}", id);
    }

    @Transactional
    public void toggleActivation(Long id) {
        Utilisateur current = securityService.getCurrentUser();
        Utilisateur utilisateur = findById(id);

        if (current.getId().equals(id)) {
            throw new BusinessException("Impossible de modifier l'activation de son propre compte");
        }

        if (utilisateur.getRole() == Role.SUPER_ADMIN && current.getRole() != Role.SUPER_ADMIN) {
            throw new ForbiddenException("Seul un SUPER_ADMIN peut modifier un autre SUPER_ADMIN");
        }

        if (utilisateur.getRole() == Role.SUPER_ADMIN && utilisateur.isActif()) {
            securityService.verifierMinimumSuperAdmin();
        }

        utilisateur.setActif(!utilisateur.isActif());
        utilisateurRepository.save(utilisateur);

        auditLogService.logAccountToggle(current.getEmail(), utilisateur.getEmail(), utilisateur.isActif());
        log.info("Utilisateur {} {}", id, utilisateur.isActif() ? "activé" : "désactivé");
    }

    @Transactional
    public void changeRole(Long id, Role nouveauRole, String motif) {
        Utilisateur current = securityService.getCurrentUser();
        Utilisateur target = findById(id);

        if (current.getId().equals(id)) {
            throw new BusinessException("Impossible de modifier son propre rôle");
        }

        if (nouveauRole == Role.SUPER_ADMIN) {
            throw new ForbiddenException("Le rôle SUPER_ADMIN ne peut pas être attribué via l'API");
        }

        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenException("Impossible de modifier le rôle d'un SUPER_ADMIN");
        }

        Role ancienRole = target.getRole();
        if (ancienRole == nouveauRole) {
            throw new BusinessException("L'utilisateur a déjà le rôle " + nouveauRole);
        }

        target.setRole(nouveauRole);

        gererChangementProfil(target, ancienRole, nouveauRole);

        utilisateurRepository.save(target);

        String details = String.format("Ancien=%s, Nouveau=%s", ancienRole.name(), nouveauRole.name());
        if (motif != null && !motif.isBlank()) {
            details += ", Motif=" + motif;
        }
        auditLogService.logAction(
                com.academiq.entity.AuditAction.CHANGEMENT_ROLE,
                current.getEmail(), target.getEmail(), details
        );
        log.info("Rôle de l'utilisateur {} changé de {} en {} (motif: {})", id, ancienRole, nouveauRole, motif);
    }

    public long countByRole(Role role) {
        return utilisateurRepository.countByRole(role);
    }

    public long countActifs() {
        return utilisateurRepository.countByActifTrue();
    }

    public Page<Utilisateur> rechercher(String keyword, Pageable pageable) {
        return utilisateurRepository.rechercher(keyword, pageable);
    }

    private void gererChangementProfil(Utilisateur utilisateur, Role ancienRole, Role nouveauRole) {
        if (ancienRole == nouveauRole) return;

        // Supprimer l'ancien profil
        switch (ancienRole) {
            case ETUDIANT -> etudiantRepository.findByUtilisateurId(utilisateur.getId())
                    .ifPresent(etudiantRepository::delete);
            case ENSEIGNANT -> enseignantRepository.findByUtilisateurId(utilisateur.getId())
                    .ifPresent(enseignantRepository::delete);
            case ADMIN, SUPER_ADMIN -> adminRepository.findByUtilisateurId(utilisateur.getId())
                    .ifPresent(adminRepository::delete);
            default -> { }
        }

        // Créer le nouveau profil
        switch (nouveauRole) {
            case ETUDIANT -> {
                Etudiant etudiant = Etudiant.builder()
                        .utilisateur(utilisateur)
                        .matricule(generateMatricule("ETU"))
                        .dateInscription(LocalDate.now())
                        .statut(StatutEtudiant.ACTIF)
                        .build();
                etudiantRepository.save(etudiant);
            }
            case ENSEIGNANT -> {
                Enseignant enseignant = Enseignant.builder()
                        .utilisateur(utilisateur)
                        .matricule(generateMatricule("ENS"))
                        .specialite("Non définie")
                        .dateRecrutement(LocalDate.now())
                        .statut(StatutEnseignant.ACTIF)
                        .build();
                enseignantRepository.save(enseignant);
            }
            case ADMIN -> {
                Admin admin = Admin.builder()
                        .utilisateur(utilisateur)
                        .fonction("Administrateur")
                        .niveau(NiveauAdmin.ADMIN)
                        .build();
                adminRepository.save(admin);
            }
            case SUPER_ADMIN -> {
                Admin admin = Admin.builder()
                        .utilisateur(utilisateur)
                        .fonction("Super Administrateur")
                        .niveau(NiveauAdmin.SUPER_ADMIN)
                        .build();
                adminRepository.save(admin);
            }
            default -> { }
        }
    }

    private void createEtudiantProfil(Utilisateur utilisateur, UtilisateurCreateRequest request) {
        String matricule = request.getMatriculeEtudiant();
        if (matricule == null || matricule.isBlank()) {
            matricule = generateMatricule("ETU");
        }
        if (etudiantRepository.existsByMatricule(matricule)) {
            throw new DuplicateResourceException("Etudiant", "matricule", matricule);
        }

        Etudiant etudiant = Etudiant.builder()
                .utilisateur(utilisateur)
                .matricule(matricule)
                .dateInscription(LocalDate.now())
                .niveauActuel(request.getNiveauActuel())
                .filiereActuelle(request.getFiliereActuelle())
                .numeroTuteur(request.getNumeroTuteur())
                .nomTuteur(request.getNomTuteur())
                .statut(StatutEtudiant.ACTIF)
                .build();

        etudiantRepository.save(etudiant);
    }

    private void createEnseignantProfil(Utilisateur utilisateur, UtilisateurCreateRequest request) {
        String matricule = request.getMatriculeEnseignant();
        if (matricule == null || matricule.isBlank()) {
            matricule = generateMatricule("ENS");
        }
        if (enseignantRepository.existsByMatricule(matricule)) {
            throw new DuplicateResourceException("Enseignant", "matricule", matricule);
        }

        Enseignant enseignant = Enseignant.builder()
                .utilisateur(utilisateur)
                .matricule(matricule)
                .specialite(request.getSpecialite() != null ? request.getSpecialite() : "Non définie")
                .grade(request.getGrade())
                .departement(request.getDepartement())
                .bureau(request.getBureau())
                .dateRecrutement(LocalDate.now())
                .statut(StatutEnseignant.ACTIF)
                .build();

        enseignantRepository.save(enseignant);
    }

    private void createAdminProfil(Utilisateur utilisateur, UtilisateurCreateRequest request) {
        Admin admin = Admin.builder()
                .utilisateur(utilisateur)
                .fonction(request.getFonction() != null ? request.getFonction() : "Administrateur")
                .departement(request.getDepartementAdmin())
                .niveau(request.getNiveauAdmin())
                .build();

        adminRepository.save(admin);
    }

    private void updateEtudiantProfil(Utilisateur utilisateur, UtilisateurUpdateRequest request) {
        etudiantRepository.findByUtilisateurId(utilisateur.getId()).ifPresent(etudiant -> {
            if (request.getNiveauActuel() != null) {
                etudiant.setNiveauActuel(request.getNiveauActuel());
            }
            if (request.getFiliereActuelle() != null) {
                etudiant.setFiliereActuelle(request.getFiliereActuelle());
            }
            if (request.getNumeroTuteur() != null) {
                etudiant.setNumeroTuteur(request.getNumeroTuteur());
            }
            if (request.getNomTuteur() != null) {
                etudiant.setNomTuteur(request.getNomTuteur());
            }
            if (request.getStatutEtudiant() != null) {
                etudiant.setStatut(request.getStatutEtudiant());
            }
            etudiantRepository.save(etudiant);
        });
    }

    private void updateEnseignantProfil(Utilisateur utilisateur, UtilisateurUpdateRequest request) {
        enseignantRepository.findByUtilisateurId(utilisateur.getId()).ifPresent(enseignant -> {
            if (request.getSpecialite() != null) {
                enseignant.setSpecialite(request.getSpecialite());
            }
            if (request.getGrade() != null) {
                enseignant.setGrade(request.getGrade());
            }
            if (request.getDepartement() != null) {
                enseignant.setDepartement(request.getDepartement());
            }
            if (request.getBureau() != null) {
                enseignant.setBureau(request.getBureau());
            }
            if (request.getStatutEnseignant() != null) {
                enseignant.setStatut(request.getStatutEnseignant());
            }
            enseignantRepository.save(enseignant);
        });
    }

    private void updateAdminProfil(Utilisateur utilisateur, UtilisateurUpdateRequest request) {
        adminRepository.findByUtilisateurId(utilisateur.getId()).ifPresent(admin -> {
            if (request.getFonction() != null) {
                admin.setFonction(request.getFonction());
            }
            if (request.getDepartementAdmin() != null) {
                admin.setDepartement(request.getDepartementAdmin());
            }
            if (request.getNiveauAdmin() != null) {
                admin.setNiveau(request.getNiveauAdmin());
            }
            adminRepository.save(admin);
        });
    }

    private String generateMatricule(String prefix) {
        String year = String.valueOf(Year.now().getValue());
        String unique = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + year + "-" + unique;
    }
}
