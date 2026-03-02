package com.academiq.service;

import com.academiq.entity.Evaluation;
import com.academiq.entity.StatutEvaluation;
import com.academiq.entity.StatutInscription;
import com.academiq.exception.BadRequestException;
import com.academiq.repository.InscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoteValidationService {

    private static final Logger log = LoggerFactory.getLogger(NoteValidationService.class);

    public void validerNote(Double valeur, double noteMaximale, boolean absent) {
        if (absent) {
            if (valeur != null) {
                log.warn("Note ignorée pour un étudiant absent (valeur : {})", valeur);
            }
            return;
        }
        if (valeur == null) {
            throw new BadRequestException("La note est obligatoire pour un étudiant présent");
        }
        if (valeur < 0) {
            throw new BadRequestException("La note ne peut pas être négative");
        }
        if (valeur > noteMaximale) {
            throw new BadRequestException("La note ne peut pas dépasser " + noteMaximale);
        }
    }

    public void validerEvaluationModifiable(Evaluation evaluation) {
        if (evaluation.getStatut() == StatutEvaluation.VERROUILLEE) {
            throw new BadRequestException("Cette évaluation est verrouillée, aucune modification possible");
        }
    }

    public void validerEtudiantInscrit(Long etudiantId, Long promotionId, InscriptionRepository inscriptionRepo) {
        boolean inscrit = inscriptionRepo.findByEtudiantIdAndPromotionId(etudiantId, promotionId)
                .filter(i -> i.getStatut() == StatutInscription.ACTIVE)
                .isPresent();
        if (!inscrit) {
            throw new BadRequestException("L'étudiant n'est pas inscrit à cette promotion");
        }
    }
}
