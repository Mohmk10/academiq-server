package com.academiq.dto.utilisateur;

import com.academiq.entity.NiveauAdmin;
import com.academiq.entity.StatutEnseignant;
import com.academiq.entity.StatutEtudiant;
import jakarta.validation.constraints.Email;
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
public class UtilisateurUpdateRequest {

    @Email(message = "Format d'email invalide")
    private String email;

    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String prenom;

    @Pattern(regexp = "^\\+221(7[0-8]|33|30)[0-9]{7}$", message = "Format attendu : +221XXXXXXXXX (numéro sénégalais valide)")
    private String telephone;
    private LocalDate dateNaissance;
    private String adresse;
    private String photoProfil;

    // Champs spécifiques Etudiant
    private String niveauActuel;
    private String filiereActuelle;
    @Pattern(regexp = "^\\+221(7[0-8]|33|30)[0-9]{7}$", message = "Format attendu : +221XXXXXXXXX (numéro sénégalais valide)")
    private String numeroTuteur;
    private String nomTuteur;
    private StatutEtudiant statutEtudiant;

    // Champs spécifiques Enseignant
    private String specialite;
    private String grade;
    private String departement;
    private String bureau;
    private StatutEnseignant statutEnseignant;

    // Champs spécifiques Admin
    private String fonction;
    private String departementAdmin;
    private NiveauAdmin niveauAdmin;
}
