package com.academiq.service;

import com.academiq.entity.Alerte;
import com.academiq.entity.Enseignant;
import com.academiq.entity.NiveauAlerte;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void notifierAlerte(Alerte alerte) {
        String etudiantNom = alerte.getEtudiant().getUtilisateur().getPrenom()
                + " " + alerte.getEtudiant().getUtilisateur().getNom();

        log.info("NOTIFICATION — Alerte {} ({}) pour l'étudiant {} : {}",
                alerte.getType(), alerte.getNiveau(), etudiantNom, alerte.getMessage());

        if (alerte.getNiveau() == NiveauAlerte.CRITIQUE) {
            log.warn("ALERTE CRITIQUE — {} — {} : {}",
                    alerte.getType(), etudiantNom, alerte.getMessage());
        }
    }

    public void notifierEnseignant(Alerte alerte, Enseignant enseignant) {
        String enseignantNom = enseignant.getUtilisateur().getPrenom()
                + " " + enseignant.getUtilisateur().getNom();
        String etudiantNom = alerte.getEtudiant().getUtilisateur().getPrenom()
                + " " + alerte.getEtudiant().getUtilisateur().getNom();
        String moduleNom = alerte.getModuleFormation() != null
                ? alerte.getModuleFormation().getNom() : "N/A";

        log.info("NOTIFICATION ENSEIGNANT — {} : alerte pour {} dans {}",
                enseignantNom, etudiantNom, moduleNom);
    }

    public void notifierResponsable(List<Alerte> alertes) {
        log.info("NOTIFICATION RESPONSABLE — {} nouvelles alertes pédagogiques", alertes.size());
    }

    private String construireMessageEmail(Alerte alerte) {
        String etudiantNom = alerte.getEtudiant().getUtilisateur().getPrenom()
                + " " + alerte.getEtudiant().getUtilisateur().getNom();

        return String.format("""
                Alerte Pédagogique - %s

                Niveau : %s
                Étudiant : %s

                %s

                Valeur détectée : %s
                Seuil : %s
                """,
                alerte.getTitre(),
                alerte.getNiveau(),
                etudiantNom,
                alerte.getMessage(),
                alerte.getValeurDetectee() != null ? alerte.getValeurDetectee().toString() : "N/A",
                alerte.getSeuilAlerte() != null ? alerte.getSeuilAlerte().toString() : "N/A");
    }
}
