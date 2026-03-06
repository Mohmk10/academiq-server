package com.academiq.dto.utilisateur;

import com.academiq.entity.NiveauAdmin;
import com.academiq.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String nom;

    @NotBlank
    @Size(max = 100)
    private String prenom;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String motDePasse;

    @NotNull
    private Role role;

    @Pattern(regexp = "^\\+221(7[0-8]|33|30)[0-9]{7}$", message = "Format attendu : +221XXXXXXXXX (numéro sénégalais valide)")
    private String telephone;
    private LocalDate dateNaissance;
    private String adresse;

    // Champs spécifiques Etudiant
    private String matriculeEtudiant;
    private String niveauActuel;
    private String filiereActuelle;
    @Pattern(regexp = "^\\+221(7[0-8]|33|30)[0-9]{7}$", message = "Format attendu : +221XXXXXXXXX (numéro sénégalais valide)")
    private String numeroTuteur;
    private String nomTuteur;

    // Champs spécifiques Enseignant
    private String matriculeEnseignant;
    private String specialite;
    private String grade;
    private String departement;
    private String bureau;

    // Champs spécifiques Admin
    private String fonction;
    private String departementAdmin;
    private NiveauAdmin niveauAdmin;
}
