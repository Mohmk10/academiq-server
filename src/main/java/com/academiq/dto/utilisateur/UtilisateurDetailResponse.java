package com.academiq.dto.utilisateur;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurDetailResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String telephone;
    private String adresse;
    private String photoProfil;
    private LocalDate dateNaissance;
    private boolean actif;
    private LocalDateTime dernierLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private EtudiantInfo etudiant;
    private EnseignantInfo enseignant;
    private AdminInfo admin;
}
