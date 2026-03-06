package com.academiq.service;

import com.academiq.dto.utilisateur.ImportResult;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Role;
import com.academiq.entity.StatutEtudiant;
import com.academiq.entity.Utilisateur;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);
    private static final String SEPARATEUR = ";";
    private static final String REGEX_TELEPHONE_SN = "^\\+221(7[0-8]|33|30)[0-9]{7}$";
    private static final String MOT_DE_PASSE_DEFAUT = "Academiq2026!";

    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ImportResult importerEtudiantsCSV(MultipartFile fichier) {
        ImportResult result = ImportResult.builder()
                .totalLignes(0)
                .importes(0)
                .echecs(0)
                .build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(fichier.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.addErreur("Fichier vide");
                return result;
            }

            String ligne;
            int numeroLigne = 1;

            while ((ligne = reader.readLine()) != null) {
                numeroLigne++;
                result.setTotalLignes(result.getTotalLignes() + 1);

                if (ligne.trim().isEmpty()) {
                    continue;
                }

                try {
                    importerLigneEtudiant(ligne, numeroLigne);
                    result.setImportes(result.getImportes() + 1);
                } catch (Exception e) {
                    result.setEchecs(result.getEchecs() + 1);
                    result.addErreur("Ligne " + numeroLigne + " : " + e.getMessage());
                    log.warn("Erreur import ligne {} : {}", numeroLigne, e.getMessage());
                }
            }

        } catch (Exception e) {
            result.addErreur("Erreur de lecture du fichier : " + e.getMessage());
            log.error("Erreur lors de l'import CSV", e);
        }

        log.info("Import terminé : {} importés, {} échecs sur {} lignes",
                result.getImportes(), result.getEchecs(), result.getTotalLignes());
        return result;
    }

    private void importerLigneEtudiant(String ligne, int numeroLigne) {
        String[] colonnes = ligne.split(SEPARATEUR, -1);

        if (colonnes.length < 4) {
            throw new IllegalArgumentException("Format invalide, minimum 4 colonnes requises (nom;prenom;email;niveauActuel)");
        }

        String nom = colonnes[0].trim();
        String prenom = colonnes[1].trim();
        String email = colonnes[2].trim();
        String niveauActuel = colonnes[3].trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Nom, prénom et email sont obligatoires");
        }

        if (utilisateurRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email déjà existant : " + email);
        }

        String filiereActuelle = colonnes.length > 4 ? colonnes[4].trim() : null;
        String numeroTuteur = colonnes.length > 5 ? colonnes[5].trim() : null;
        String nomTuteur = colonnes.length > 6 ? colonnes[6].trim() : null;

        if (numeroTuteur != null && !numeroTuteur.isEmpty() && !numeroTuteur.matches(REGEX_TELEPHONE_SN)) {
            throw new IllegalArgumentException("Numéro tuteur invalide : " + numeroTuteur + ". Format attendu : +221XXXXXXXXX");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_DEFAUT))
                .role(Role.ETUDIANT)
                .actif(true)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        String matricule = generateMatricule();

        Etudiant etudiant = Etudiant.builder()
                .utilisateur(utilisateur)
                .matricule(matricule)
                .dateInscription(LocalDate.now())
                .niveauActuel(niveauActuel.isEmpty() ? null : niveauActuel)
                .filiereActuelle(filiereActuelle != null && !filiereActuelle.isEmpty() ? filiereActuelle : null)
                .numeroTuteur(numeroTuteur != null && !numeroTuteur.isEmpty() ? numeroTuteur : null)
                .nomTuteur(nomTuteur != null && !nomTuteur.isEmpty() ? nomTuteur : null)
                .statut(StatutEtudiant.ACTIF)
                .build();

        etudiantRepository.save(etudiant);
    }

    private String generateMatricule() {
        String year = String.valueOf(Year.now().getValue());
        String unique = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ETU-" + year + "-" + unique;
    }
}
