package com.academiq.service;

import com.academiq.entity.DecisionJury;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Mention;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Note;
import com.academiq.entity.TypeEvaluation;
import com.academiq.entity.Niveau;
import com.academiq.entity.Semestre;
import com.academiq.entity.UniteEnseignement;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.NoteRepository;
import com.academiq.repository.NiveauRepository;
import com.academiq.repository.SemestreRepository;
import com.academiq.repository.UniteEnseignementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Service centralisé pour tous les calculs académiques :
 * moyennes, crédits, décisions de jury et mentions.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalculService {

    private static final Logger log = LoggerFactory.getLogger(CalculService.class);

    private final NoteRepository noteRepository;
    private final EvaluationRepository evaluationRepository;
    private final ModuleFormationRepository moduleFormationRepository;
    private final UniteEnseignementRepository uniteEnseignementRepository;
    private final SemestreRepository semestreRepository;
    private final NiveauRepository niveauRepository;
    private final InscriptionRepository inscriptionRepository;

    private static final Set<TypeEvaluation> TYPES_CC = EnumSet.of(
            TypeEvaluation.CC, TypeEvaluation.TP, TypeEvaluation.PROJET, TypeEvaluation.ORAL);

    private static final Set<TypeEvaluation> TYPES_EXAMEN = EnumSet.of(
            TypeEvaluation.EXAMEN, TypeEvaluation.PARTIEL);

    /**
     * Arrondit une valeur au nombre de décimales spécifié (arrondi demi-supérieur).
     */
    private double arrondir(double valeur, int decimales) {
        return BigDecimal.valueOf(valeur)
                .setScale(decimales, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Calcule la moyenne d'un étudiant pour un module donné dans une promotion.
     * Applique la pondération CC/Examen définie sur le module.
     *
     * @return la moyenne arrondie à 2 décimales, ou null si aucune note
     */
    public Double calculerMoyenneModule(Long etudiantId, Long moduleId, Long promotionId) {
        ModuleFormation module = moduleFormationRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        List<Evaluation> evaluations = evaluationRepository
                .findByModuleFormationIdAndPromotionId(moduleId, promotionId);

        if (evaluations.isEmpty()) {
            return null;
        }

        double sommeCoeffCC = 0;
        double sommeValeurCoeffCC = 0;
        boolean hasCC = false;

        double sommeCoeffExamen = 0;
        double sommeValeurCoeffExamen = 0;
        boolean hasExamen = false;

        Double noteRattrapage = null;
        double coeffRattrapage = 0;

        for (Evaluation eval : evaluations) {
            Note note = noteRepository.findByEtudiantIdAndEvaluationId(etudiantId, eval.getId())
                    .orElse(null);

            if (note == null) continue;

            double valeur;
            if (note.isAbsent() && note.getValeur() == null) {
                valeur = 0;
            } else if (note.getValeur() != null) {
                valeur = (note.getValeur() / eval.getNoteMaximale()) * 20;
            } else {
                continue;
            }

            if (eval.getType() == TypeEvaluation.RATTRAPAGE) {
                noteRattrapage = valeur;
                coeffRattrapage = eval.getCoefficient();
            } else if (TYPES_CC.contains(eval.getType())) {
                sommeValeurCoeffCC += valeur * eval.getCoefficient();
                sommeCoeffCC += eval.getCoefficient();
                hasCC = true;
            } else if (TYPES_EXAMEN.contains(eval.getType())) {
                sommeValeurCoeffExamen += valeur * eval.getCoefficient();
                sommeCoeffExamen += eval.getCoefficient();
                hasExamen = true;
            }
        }

        if (!hasCC && !hasExamen && noteRattrapage == null) {
            return null;
        }

        Double moyenneCC = hasCC ? sommeValeurCoeffCC / sommeCoeffCC : null;

        Double noteExamen;
        if (hasExamen) {
            noteExamen = sommeValeurCoeffExamen / sommeCoeffExamen;
            if (noteRattrapage != null && noteRattrapage > noteExamen) {
                noteExamen = noteRattrapage;
            }
        } else if (noteRattrapage != null) {
            noteExamen = noteRattrapage;
        } else {
            noteExamen = null;
        }

        double moyenne;
        if (moyenneCC != null && noteExamen != null) {
            moyenne = moyenneCC * module.getPonderationCC() + noteExamen * module.getPonderationExamen();
        } else if (moyenneCC != null) {
            moyenne = moyenneCC;
        } else {
            moyenne = noteExamen;
        }

        return arrondir(moyenne, 2);
    }

    /**
     * Calcule la moyenne d'un étudiant pour une UE (moyenne pondérée des modules).
     *
     * @return la moyenne arrondie à 2 décimales, ou null si aucun module n'a de note
     */
    public Double calculerMoyenneUE(Long etudiantId, Long ueId, Long promotionId) {
        UniteEnseignement ue = uniteEnseignementRepository.findById(ueId)
                .orElseThrow(() -> new ResourceNotFoundException("Unité d'enseignement", "id", ueId));

        List<ModuleFormation> modules = moduleFormationRepository.findByUniteEnseignementId(ueId);

        double sommeCoeff = 0;
        double sommeValeurCoeff = 0;

        for (ModuleFormation module : modules) {
            Double moyenneModule = calculerMoyenneModule(etudiantId, module.getId(), promotionId);
            if (moyenneModule != null) {
                sommeValeurCoeff += moyenneModule * module.getCoefficient();
                sommeCoeff += module.getCoefficient();
            }
        }

        if (sommeCoeff == 0) {
            return null;
        }

        return arrondir(sommeValeurCoeff / sommeCoeff, 2);
    }

    /**
     * Vérifie si une UE est validée (moyenne >= 10/20).
     */
    public boolean isUEValidee(Long etudiantId, Long ueId, Long promotionId) {
        Double moyenne = calculerMoyenneUE(etudiantId, ueId, promotionId);
        return moyenne != null && moyenne >= 10.0;
    }

    /**
     * Calcule la moyenne semestrielle (moyenne pondérée des UEs par leurs coefficients).
     *
     * @return la moyenne arrondie à 2 décimales, ou null si aucune UE n'a de note
     */
    public Double calculerMoyenneSemestre(Long etudiantId, Long semestreId, Long promotionId) {
        Semestre semestre = semestreRepository.findById(semestreId)
                .orElseThrow(() -> new ResourceNotFoundException("Semestre", "id", semestreId));

        List<UniteEnseignement> ues = uniteEnseignementRepository.findBySemestreId(semestreId);

        double sommeCoeff = 0;
        double sommeValeurCoeff = 0;

        for (UniteEnseignement ue : ues) {
            Double moyenneUE = calculerMoyenneUE(etudiantId, ue.getId(), promotionId);
            if (moyenneUE != null) {
                sommeValeurCoeff += moyenneUE * ue.getCoefficient();
                sommeCoeff += ue.getCoefficient();
            }
        }

        if (sommeCoeff == 0) {
            return null;
        }

        return arrondir(sommeValeurCoeff / sommeCoeff, 2);
    }

    /**
     * Calcule la moyenne annuelle d'un étudiant pour un niveau donné.
     * Pondère les semestres par le total des crédits de leurs UEs.
     *
     * @return la moyenne arrondie à 2 décimales, ou null si aucun semestre n'a de note
     */
    public Double calculerMoyenneAnnuelle(Long etudiantId, Long niveauId, Long promotionId) {
        Niveau niveau = niveauRepository.findById(niveauId)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", "id", niveauId));

        List<Semestre> semestres = semestreRepository.findByNiveauId(niveauId);

        double sommeCredits = 0;
        double sommeValeurCredits = 0;

        for (Semestre semestre : semestres) {
            Double moyenneSemestre = calculerMoyenneSemestre(etudiantId, semestre.getId(), promotionId);
            if (moyenneSemestre != null) {
                int creditsSemestre = calculerCreditsTotauxSemestre(semestre.getId());
                sommeValeurCredits += moyenneSemestre * creditsSemestre;
                sommeCredits += creditsSemestre;
            }
        }

        if (sommeCredits == 0) {
            return null;
        }

        return arrondir(sommeValeurCredits / sommeCredits, 2);
    }

    /**
     * Calcule le total des crédits des UEs d'un semestre.
     */
    private int calculerCreditsTotauxSemestre(Long semestreId) {
        List<UniteEnseignement> ues = uniteEnseignementRepository.findBySemestreId(semestreId);
        return ues.stream().mapToInt(UniteEnseignement::getCredits).sum();
    }

    /**
     * Calcule le nombre de crédits ECTS validés pour un semestre.
     * Une UE est validée si sa moyenne est >= 10/20.
     */
    public int calculerCreditsValides(Long etudiantId, Long semestreId, Long promotionId) {
        List<UniteEnseignement> ues = uniteEnseignementRepository.findBySemestreId(semestreId);
        int credits = 0;
        for (UniteEnseignement ue : ues) {
            if (isUEValidee(etudiantId, ue.getId(), promotionId)) {
                credits += ue.getCredits();
            }
        }
        return credits;
    }

    /**
     * Calcule le total des crédits validés sur l'ensemble des semestres d'un niveau.
     */
    public int calculerCreditsAnnuels(Long etudiantId, Long niveauId, Long promotionId) {
        List<Semestre> semestres = semestreRepository.findByNiveauId(niveauId);
        int total = 0;
        for (Semestre semestre : semestres) {
            total += calculerCreditsValides(etudiantId, semestre.getId(), promotionId);
        }
        return total;
    }

    /**
     * Retourne le nombre de crédits requis pour valider un niveau.
     */
    public int calculerCreditsTotauxRequis(Long niveauId) {
        Niveau niveau = niveauRepository.findById(niveauId)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", "id", niveauId));
        return niveau.getCreditsRequis();
    }

    /**
     * Détermine la décision du jury pour un étudiant à un niveau donné.
     * Basée sur la moyenne annuelle et les crédits validés.
     */
    public DecisionJury determinerDecision(Long etudiantId, Long niveauId, Long promotionId) {
        Double moyenne = calculerMoyenneAnnuelle(etudiantId, niveauId, promotionId);

        if (moyenne == null) {
            return DecisionJury.EN_ATTENTE;
        }

        int creditsValides = calculerCreditsAnnuels(etudiantId, niveauId, promotionId);
        int creditsRequis = calculerCreditsTotauxRequis(niveauId);

        if (moyenne >= 10 && creditsValides >= creditsRequis) {
            return DecisionJury.ADMIS;
        }

        if (moyenne >= 10 && creditsRequis > 0 && creditsValides >= (creditsRequis * 0.8)) {
            return DecisionJury.ADMIS_COMPENSATION;
        }

        if (moyenne >= 8) {
            return DecisionJury.RATTRAPAGE;
        }

        return DecisionJury.AJOURNE;
    }

    /**
     * Calcule séparément la moyenne CC et la note examen pour un module.
     * Retourne un tableau [moyenneCC, noteExamen, moyenneModule] (chaque élément peut être null).
     */
    public Double[] calculerDetailsModule(Long etudiantId, Long moduleId, Long promotionId) {
        ModuleFormation module = moduleFormationRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        List<Evaluation> evaluations = evaluationRepository
                .findByModuleFormationIdAndPromotionId(moduleId, promotionId);

        double sommeCoeffCC = 0, sommeValeurCoeffCC = 0;
        boolean hasCC = false;
        double sommeCoeffExamen = 0, sommeValeurCoeffExamen = 0;
        boolean hasExamen = false;
        Double noteRattrapage = null;

        for (Evaluation eval : evaluations) {
            Note note = noteRepository.findByEtudiantIdAndEvaluationId(etudiantId, eval.getId())
                    .orElse(null);
            if (note == null) continue;

            double valeur;
            if (note.isAbsent() && note.getValeur() == null) {
                valeur = 0;
            } else if (note.getValeur() != null) {
                valeur = (note.getValeur() / eval.getNoteMaximale()) * 20;
            } else {
                continue;
            }

            if (eval.getType() == TypeEvaluation.RATTRAPAGE) {
                noteRattrapage = valeur;
            } else if (TYPES_CC.contains(eval.getType())) {
                sommeValeurCoeffCC += valeur * eval.getCoefficient();
                sommeCoeffCC += eval.getCoefficient();
                hasCC = true;
            } else if (TYPES_EXAMEN.contains(eval.getType())) {
                sommeValeurCoeffExamen += valeur * eval.getCoefficient();
                sommeCoeffExamen += eval.getCoefficient();
                hasExamen = true;
            }
        }

        Double moyenneCC = hasCC ? arrondir(sommeValeurCoeffCC / sommeCoeffCC, 2) : null;

        Double noteExamen;
        if (hasExamen) {
            noteExamen = arrondir(sommeValeurCoeffExamen / sommeCoeffExamen, 2);
            if (noteRattrapage != null && noteRattrapage > noteExamen) {
                noteExamen = arrondir(noteRattrapage, 2);
            }
        } else if (noteRattrapage != null) {
            noteExamen = arrondir(noteRattrapage, 2);
        } else {
            noteExamen = null;
        }

        Double moyenneModule;
        if (moyenneCC != null && noteExamen != null) {
            moyenneModule = arrondir(
                    moyenneCC * module.getPonderationCC() + noteExamen * module.getPonderationExamen(), 2);
        } else if (moyenneCC != null) {
            moyenneModule = moyenneCC;
        } else if (noteExamen != null) {
            moyenneModule = noteExamen;
        } else {
            moyenneModule = null;
        }

        return new Double[]{moyenneCC, noteExamen, moyenneModule};
    }

    /**
     * Détermine la mention en fonction de la moyenne.
     *
     * @return la mention ou null si la moyenne est insuffisante (< 10)
     */
    public Mention determinerMention(Double moyenne) {
        if (moyenne == null || moyenne < 10) {
            return null;
        }
        if (moyenne >= 18) return Mention.EXCELLENT;
        if (moyenne >= 16) return Mention.TRES_BIEN;
        if (moyenne >= 14) return Mention.BIEN;
        if (moyenne >= 12) return Mention.ASSEZ_BIEN;
        return Mention.PASSABLE;
    }
}
