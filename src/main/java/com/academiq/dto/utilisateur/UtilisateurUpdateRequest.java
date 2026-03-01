package com.academiq.dto.utilisateur;

import com.academiq.entity.NiveauAdmin;
import com.academiq.entity.StatutEnseignant;
import com.academiq.entity.StatutEtudiant;
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

    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String prenom;

    private String telephone;
    private LocalDate dateNaissance;
    private String adresse;
    private String photoProfil;

    // Champs spécifiques Etudiant
    private String niveauActuel;
    private String filiereActuelle;
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
