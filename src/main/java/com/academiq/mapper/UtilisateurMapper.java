package com.academiq.mapper;

import com.academiq.dto.auth.UtilisateurResponse;
import com.academiq.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    public UtilisateurResponse toResponse(Utilisateur utilisateur) {
        return UtilisateurResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().name())
                .telephone(utilisateur.getTelephone())
                .dateNaissance(utilisateur.getDateNaissance())
                .adresse(utilisateur.getAdresse())
                .photoProfil(utilisateur.getPhotoProfil())
                .actif(utilisateur.isActif())
                .dernierLogin(utilisateur.getDernierLogin())
                .createdAt(utilisateur.getCreatedAt())
                .build();
    }
}
