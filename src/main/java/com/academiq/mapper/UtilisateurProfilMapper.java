package com.academiq.mapper;

import com.academiq.dto.utilisateur.AdminInfo;
import com.academiq.dto.utilisateur.EnseignantInfo;
import com.academiq.dto.utilisateur.EtudiantInfo;
import com.academiq.dto.utilisateur.UtilisateurDetailResponse;
import com.academiq.dto.utilisateur.UtilisateurSummaryResponse;
import com.academiq.entity.Admin;
import com.academiq.entity.Enseignant;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Utilisateur;
import com.academiq.repository.AdminRepository;
import com.academiq.repository.EnseignantRepository;
import com.academiq.repository.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UtilisateurProfilMapper {

    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final AdminRepository adminRepository;

    public UtilisateurDetailResponse toDetailResponse(Utilisateur u) {
        UtilisateurDetailResponse.UtilisateurDetailResponseBuilder builder = UtilisateurDetailResponse.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .role(u.getRole().name())
                .telephone(u.getTelephone())
                .adresse(u.getAdresse())
                .photoProfil(u.getPhotoProfil())
                .dateNaissance(u.getDateNaissance())
                .actif(u.isActif())
                .dernierLogin(u.getDernierLogin())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt());

        switch (u.getRole()) {
            case ETUDIANT -> etudiantRepository.findByUtilisateurId(u.getId())
                    .ifPresent(e -> builder.etudiant(toEtudiantInfo(e)));
            case ENSEIGNANT -> enseignantRepository.findByUtilisateurId(u.getId())
                    .ifPresent(e -> builder.enseignant(toEnseignantInfo(e)));
            case ADMIN -> adminRepository.findByUtilisateurId(u.getId())
                    .ifPresent(a -> builder.admin(toAdminInfo(a)));
            default -> { }
        }

        return builder.build();
    }

    public UtilisateurSummaryResponse toSummaryResponse(Utilisateur u) {
        String matricule = resolveMatricule(u);

        return UtilisateurSummaryResponse.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .role(u.getRole().name())
                .actif(u.isActif())
                .matricule(matricule)
                .build();
    }

    public List<UtilisateurSummaryResponse> toSummaryList(List<Utilisateur> utilisateurs) {
        return utilisateurs.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    private String resolveMatricule(Utilisateur u) {
        return switch (u.getRole()) {
            case ETUDIANT -> etudiantRepository.findByUtilisateurId(u.getId())
                    .map(Etudiant::getMatricule)
                    .orElse(null);
            case ENSEIGNANT -> enseignantRepository.findByUtilisateurId(u.getId())
                    .map(Enseignant::getMatricule)
                    .orElse(null);
            default -> null;
        };
    }

    private EtudiantInfo toEtudiantInfo(Etudiant e) {
        return EtudiantInfo.builder()
                .id(e.getId())
                .matricule(e.getMatricule())
                .niveauActuel(e.getNiveauActuel())
                .filiereActuelle(e.getFiliereActuelle())
                .numeroTuteur(e.getNumeroTuteur())
                .nomTuteur(e.getNomTuteur())
                .dateInscription(e.getDateInscription())
                .statut(e.getStatut() != null ? e.getStatut().name() : null)
                .build();
    }

    private EnseignantInfo toEnseignantInfo(Enseignant e) {
        return EnseignantInfo.builder()
                .id(e.getId())
                .matricule(e.getMatricule())
                .specialite(e.getSpecialite())
                .grade(e.getGrade())
                .departement(e.getDepartement())
                .bureau(e.getBureau())
                .dateRecrutement(e.getDateRecrutement())
                .statut(e.getStatut() != null ? e.getStatut().name() : null)
                .build();
    }

    private AdminInfo toAdminInfo(Admin a) {
        return AdminInfo.builder()
                .id(a.getId())
                .fonction(a.getFonction())
                .departement(a.getDepartement())
                .niveau(a.getNiveau() != null ? a.getNiveau().name() : null)
                .build();
    }
}
