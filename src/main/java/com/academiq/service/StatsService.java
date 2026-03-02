package com.academiq.service;

import com.academiq.dto.calcul.BulletinEtudiantDTO;
import com.academiq.dto.stats.TauxReussiteDTO;
import com.academiq.entity.DecisionJury;
import com.academiq.entity.Inscription;
import com.academiq.entity.StatutInscription;
import com.academiq.repository.AlerteRepository;
import com.academiq.repository.EnseignantRepository;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.FiliereRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.NoteRepository;
import com.academiq.repository.PromotionRepository;
import com.academiq.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private final CalculService calculService;
    private final BulletinService bulletinService;
    private final NoteRepository noteRepository;
    private final EvaluationRepository evaluationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PromotionRepository promotionRepository;
    private final ModuleFormationRepository moduleFormationRepository;
    private final AlerteRepository alerteRepository;
    private final FiliereRepository filiereRepository;

    public TauxReussiteDTO calculerTauxReussiteModule(Long moduleId, Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        int totalReussis = 0;
        List<Double> moyennes = new ArrayList<>();

        for (Inscription inscription : inscriptions) {
            Double moyenne = calculService.calculerMoyenneModule(
                    inscription.getEtudiant().getId(), moduleId, promotionId);
            if (moyenne != null) {
                moyennes.add(moyenne);
                if (moyenne >= 10) {
                    totalReussis++;
                }
            }
        }

        int total = inscriptions.size();
        double taux = total > 0 ? arrondir(totalReussis * 100.0 / total) : 0;
        Double moyenneGenerale = calculerMoyenneListe(moyennes);

        return TauxReussiteDTO.builder()
                .contexte("Module")
                .totalInscrits(total)
                .totalReussis(totalReussis)
                .totalEchecs(total - totalReussis)
                .tauxReussite(taux)
                .moyenneGenerale(moyenneGenerale)
                .build();
    }

    public TauxReussiteDTO calculerTauxReussiteUE(Long ueId, Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        int totalReussis = 0;
        List<Double> moyennes = new ArrayList<>();

        for (Inscription inscription : inscriptions) {
            Double moyenne = calculService.calculerMoyenneUE(
                    inscription.getEtudiant().getId(), ueId, promotionId);
            if (moyenne != null) {
                moyennes.add(moyenne);
                if (moyenne >= 10) {
                    totalReussis++;
                }
            }
        }

        int total = inscriptions.size();
        double taux = total > 0 ? arrondir(totalReussis * 100.0 / total) : 0;
        Double moyenneGenerale = calculerMoyenneListe(moyennes);

        return TauxReussiteDTO.builder()
                .contexte("Unité d'enseignement")
                .totalInscrits(total)
                .totalReussis(totalReussis)
                .totalEchecs(total - totalReussis)
                .tauxReussite(taux)
                .moyenneGenerale(moyenneGenerale)
                .build();
    }

    public TauxReussiteDTO calculerTauxReussitePromotion(Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        int totalReussis = 0;
        List<Double> moyennes = new ArrayList<>();

        for (Inscription inscription : inscriptions) {
            try {
                BulletinEtudiantDTO bulletin = bulletinService.genererBulletin(
                        inscription.getEtudiant().getId(), promotionId);
                if (bulletin.getMoyenneAnnuelle() != null) {
                    moyennes.add(bulletin.getMoyenneAnnuelle());
                }
                if (DecisionJury.ADMIS.name().equals(bulletin.getDecision())
                        || DecisionJury.ADMIS_COMPENSATION.name().equals(bulletin.getDecision())) {
                    totalReussis++;
                }
            } catch (Exception e) {
                log.warn("Erreur bulletin étudiant {}", inscription.getEtudiant().getId(), e);
            }
        }

        int total = inscriptions.size();
        double taux = total > 0 ? arrondir(totalReussis * 100.0 / total) : 0;
        Double moyenneGenerale = calculerMoyenneListe(moyennes);

        return TauxReussiteDTO.builder()
                .contexte("Promotion")
                .totalInscrits(total)
                .totalReussis(totalReussis)
                .totalEchecs(total - totalReussis)
                .tauxReussite(taux)
                .moyenneGenerale(moyenneGenerale)
                .build();
    }

    private Double calculerMoyenneListe(List<Double> valeurs) {
        if (valeurs.isEmpty()) return null;
        double somme = valeurs.stream().mapToDouble(Double::doubleValue).sum();
        return arrondir(somme / valeurs.size());
    }

    private double arrondir(double valeur) {
        return BigDecimal.valueOf(valeur)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
