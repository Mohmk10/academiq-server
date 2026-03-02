package com.academiq.service;

import com.academiq.dto.calcul.BulletinEtudiantDTO;
import com.academiq.dto.stats.DistributionNotesDTO;
import com.academiq.dto.stats.TauxReussiteDTO;
import com.academiq.dto.stats.TrancheDTO;
import com.academiq.entity.Note;
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
import java.util.Collections;
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

    public DistributionNotesDTO calculerDistributionModule(Long moduleId, Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        List<Double> valeurs = new ArrayList<>();
        for (Inscription inscription : inscriptions) {
            Double moyenne = calculService.calculerMoyenneModule(
                    inscription.getEtudiant().getId(), moduleId, promotionId);
            if (moyenne != null) {
                valeurs.add(moyenne);
            }
        }

        return construireDistribution("Module", valeurs);
    }

    public DistributionNotesDTO calculerDistributionEvaluation(Long evaluationId) {
        List<Note> notes = noteRepository.findByEvaluationId(evaluationId);

        List<Double> valeurs = new ArrayList<>();
        for (Note note : notes) {
            if (!note.isAbsent() && note.getValeur() != null) {
                valeurs.add(note.getValeur());
            }
        }

        return construireDistribution("Évaluation", valeurs);
    }

    private DistributionNotesDTO construireDistribution(String contexte, List<Double> valeurs) {
        if (valeurs.isEmpty()) {
            return DistributionNotesDTO.builder()
                    .contexte(contexte)
                    .tranches(List.of())
                    .totalNotes(0)
                    .build();
        }

        Collections.sort(valeurs);
        int total = valeurs.size();

        double[][] bornes = {
                {0, 4}, {4, 8}, {8, 10}, {10, 12}, {12, 14}, {14, 16}, {16, 20}
        };
        String[] labels = {"0-4", "4-8", "8-10", "10-12", "12-14", "14-16", "16-20"};

        List<TrancheDTO> tranches = new ArrayList<>();
        for (int i = 0; i < bornes.length; i++) {
            double inf = bornes[i][0];
            double sup = bornes[i][1];
            int count = 0;
            for (double v : valeurs) {
                if (i == bornes.length - 1) {
                    if (v >= inf && v <= sup) count++;
                } else {
                    if (v >= inf && v < sup) count++;
                }
            }
            tranches.add(TrancheDTO.builder()
                    .label(labels[i])
                    .borneInf(inf)
                    .borneSup(sup)
                    .nombre(count)
                    .pourcentage(arrondir(count * 100.0 / total))
                    .build());
        }

        double somme = valeurs.stream().mapToDouble(Double::doubleValue).sum();
        double moyenne = somme / total;

        double mediane;
        if (total % 2 == 0) {
            mediane = (valeurs.get(total / 2 - 1) + valeurs.get(total / 2)) / 2.0;
        } else {
            mediane = valeurs.get(total / 2);
        }

        double varianceSum = valeurs.stream()
                .mapToDouble(v -> Math.pow(v - moyenne, 2))
                .sum();
        double ecartType = Math.sqrt(varianceSum / total);

        return DistributionNotesDTO.builder()
                .contexte(contexte)
                .tranches(tranches)
                .moyenne(arrondir(moyenne))
                .mediane(arrondir(mediane))
                .ecartType(arrondir(ecartType))
                .noteMin(valeurs.getFirst())
                .noteMax(valeurs.getLast())
                .totalNotes(total)
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
