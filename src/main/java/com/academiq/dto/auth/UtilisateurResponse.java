package com.academiq.dto.auth;

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
public class UtilisateurResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String telephone;
    private LocalDate dateNaissance;
    private String adresse;
    private String photoProfil;
    private boolean actif;
    private LocalDateTime dernierLogin;
    private LocalDateTime createdAt;
}
