package com.academiq.service;

import com.academiq.entity.Alerte;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Inscription;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.NiveauAlerte;
import com.academiq.entity.Note;
import com.academiq.entity.Promotion;
import com.academiq.entity.RegleAlerte;
import com.academiq.entity.StatutAlerte;
import com.academiq.entity.StatutInscription;
import com.academiq.entity.TypeAlerte;
import com.academiq.entity.TypeEvaluation;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.AlerteRepository;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.NoteRepository;
import com.academiq.repository.RegleAlerteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DetectionAlerteService {

    private static final Logger log = LoggerFactory.getLogger(DetectionAlerteService.class);

    private final RegleAlerteRepository regleAlerteRepository;
    private final AlerteRepository alerteRepository;
    private final NoteRepository noteRepository;
    private final EvaluationRepository evaluationRepository;
    private final CalculService calculService;
    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final NotificationService notificationService;

    private static final Set<TypeEvaluation> TYPES_EXAMEN = EnumSet.of(
            TypeEvaluation.EXAMEN, TypeEvaluation.PARTIEL);

    public List<Alerte> detecterAlertesMoyenneModule(Long etudiantId, Long moduleId, Long promotionId) {
        Double moyenne = calculService.calculerMoyenneModule(etudiantId, moduleId, promotionId);
        if (moyenne == null) return List.of();

        List<RegleAlerte> regles = regleAlerteRepository.findByType(TypeAlerte.MOYENNE_FAIBLE).stream()
                .filter(RegleAlerte::isActif)
                .toList();

        List<Alerte> alertes = new ArrayList<>();
        Etudiant etudiant = etudiantRepository.findById(etudiantId).orElse(null);
        if (etudiant == null) return List.of();

        for (RegleAlerte regle : regles) {
            if (moyenne < regle.getSeuil()) {
                if (alerteExisteDeja(TypeAlerte.MOYENNE_FAIBLE, etudiantId, moduleId)) continue;

                ModuleFormation module = new ModuleFormation();
                module.setId(moduleId);
                Promotion promotion = new Promotion();
                promotion.setId(promotionId);

                alertes.add(Alerte.builder()
                        .type(TypeAlerte.MOYENNE_FAIBLE)
                        .niveau(regle.getNiveauAlerte())
                        .titre(regle.getNom())
                        .message(String.format("Moyenne de %.2f/20 pour le module (seuil : %.1f/20)",
                                moyenne, regle.getSeuil()))
                        .valeurDetectee(moyenne)
                        .seuilAlerte(regle.getSeuil())
                        .etudiant(etudiant)
                        .promotion(promotion)
                        .moduleFormation(module)
                        .build());
            }
        }
        return alertes;
    }

    public List<Alerte> detecterAlertesAbsences(Long etudiantId, Long moduleId, Long promotionId) {
        List<Evaluation> evaluations = evaluationRepository
                .findByModuleFormationIdAndPromotionId(moduleId, promotionId);

        long nbAbsences = 0;
        for (Evaluation eval : evaluations) {
            Note note = noteRepository.findByEtudiantIdAndEvaluationId(etudiantId, eval.getId())
                    .orElse(null);
            if (note != null && note.isAbsent()) {
                nbAbsences++;
            }
        }

        List<RegleAlerte> regles = regleAlerteRepository.findByType(TypeAlerte.ABSENCES_REPETEES).stream()
                .filter(RegleAlerte::isActif)
                .toList();

        List<Alerte> alertes = new ArrayList<>();
        Etudiant etudiant = etudiantRepository.findById(etudiantId).orElse(null);
        if (etudiant == null) return List.of();

        for (RegleAlerte regle : regles) {
            if (regle.getNombreMaxAbsences() != null && nbAbsences >= regle.getNombreMaxAbsences()) {
                if (alerteExisteDeja(TypeAlerte.ABSENCES_REPETEES, etudiantId, moduleId)) continue;

                ModuleFormation module = new ModuleFormation();
                module.setId(moduleId);
                Promotion promotion = new Promotion();
                promotion.setId(promotionId);

                alertes.add(Alerte.builder()
                        .type(TypeAlerte.ABSENCES_REPETEES)
                        .niveau(regle.getNiveauAlerte())
                        .titre(regle.getNom())
                        .message(String.format("%d absences détectées (maximum autorisé : %d)",
                                nbAbsences, regle.getNombreMaxAbsences()))
                        .valeurDetectee((double) nbAbsences)
                        .seuilAlerte(regle.getNombreMaxAbsences().doubleValue())
                        .etudiant(etudiant)
                        .promotion(promotion)
                        .moduleFormation(module)
                        .build());
            }
        }
        return alertes;
    }

    public List<Alerte> detecterAlertesNoteEliminatoire(Long etudiantId, Long evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId).orElse(null);
        if (evaluation == null || !TYPES_EXAMEN.contains(evaluation.getType())) return List.of();

        Note note = noteRepository.findByEtudiantIdAndEvaluationId(etudiantId, evaluationId).orElse(null);
        if (note == null || note.isAbsent() || note.getValeur() == null) return List.of();

        double noteRamenee = (note.getValeur() / evaluation.getNoteMaximale()) * 20;

        List<RegleAlerte> regles = regleAlerteRepository.findByType(TypeAlerte.NOTE_ELIMINATOIRE).stream()
                .filter(RegleAlerte::isActif)
                .toList();

        List<Alerte> alertes = new ArrayList<>();
        Etudiant etudiant = etudiantRepository.findById(etudiantId).orElse(null);
        if (etudiant == null) return List.of();

        for (RegleAlerte regle : regles) {
            if (noteRamenee < regle.getSeuil()) {
                ModuleFormation module = new ModuleFormation();
                module.setId(evaluation.getModuleFormation().getId());

                alertes.add(Alerte.builder()
                        .type(TypeAlerte.NOTE_ELIMINATOIRE)
                        .niveau(regle.getNiveauAlerte())
                        .titre(regle.getNom())
                        .message(String.format("Note de %.2f/20 à l'examen '%s' (seuil éliminatoire : %.1f/20)",
                                noteRamenee, evaluation.getNom(), regle.getSeuil()))
                        .valeurDetectee(noteRamenee)
                        .seuilAlerte(regle.getSeuil())
                        .etudiant(etudiant)
                        .promotion(evaluation.getPromotion())
                        .moduleFormation(module)
                        .build());
            }
        }
        return alertes;
    }

    public List<Alerte> detecterAlertesRisqueExclusion(Long etudiantId, Long niveauId, Long promotionId) {
        int creditsValides = calculService.calculerCreditsAnnuels(etudiantId, niveauId, promotionId);

        List<RegleAlerte> regles = regleAlerteRepository.findByType(TypeAlerte.RISQUE_EXCLUSION).stream()
                .filter(RegleAlerte::isActif)
                .toList();

        List<Alerte> alertes = new ArrayList<>();
        Etudiant etudiant = etudiantRepository.findById(etudiantId).orElse(null);
        if (etudiant == null) return List.of();

        for (RegleAlerte regle : regles) {
            if (creditsValides < regle.getSeuil()) {
                if (alerteExisteDeja(TypeAlerte.RISQUE_EXCLUSION, etudiantId, null)) continue;

                Promotion promotion = new Promotion();
                promotion.setId(promotionId);

                alertes.add(Alerte.builder()
                        .type(TypeAlerte.RISQUE_EXCLUSION)
                        .niveau(regle.getNiveauAlerte())
                        .titre(regle.getNom())
                        .message(String.format("%d crédits validés (minimum requis : %.0f)",
                                creditsValides, regle.getSeuil()))
                        .valeurDetectee((double) creditsValides)
                        .seuilAlerte(regle.getSeuil())
                        .etudiant(etudiant)
                        .promotion(promotion)
                        .build());
            }
        }
        return alertes;
    }

    @Transactional
    public List<Alerte> analyserEtudiant(Long etudiantId, Long promotionId) {
        Promotion promotion = new Promotion();
        promotion.setId(promotionId);

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));

        List<Note> notes = noteRepository.findByEtudiantIdAndPromotionId(etudiantId, promotionId);
        Set<Long> moduleIds = new java.util.HashSet<>();
        Long niveauId = null;

        for (Note note : notes) {
            moduleIds.add(note.getEvaluation().getModuleFormation().getId());
            if (niveauId == null) {
                niveauId = note.getEvaluation().getPromotion().getNiveau().getId();
            }
        }

        List<Alerte> toutesAlertes = new ArrayList<>();

        for (Long moduleId : moduleIds) {
            toutesAlertes.addAll(detecterAlertesMoyenneModule(etudiantId, moduleId, promotionId));
            toutesAlertes.addAll(detecterAlertesAbsences(etudiantId, moduleId, promotionId));
        }

        if (niveauId != null) {
            toutesAlertes.addAll(detecterAlertesRisqueExclusion(etudiantId, niveauId, promotionId));
        }

        List<Alerte> sauvegardees = alerteRepository.saveAll(toutesAlertes);
        sauvegardees.forEach(notificationService::notifierAlerte);
        log.info("Analyse étudiant {} : {} alertes générées", etudiantId, sauvegardees.size());
        return sauvegardees;
    }

    @Transactional
    public List<Alerte> analyserPromotion(Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        List<Alerte> toutesAlertes = new ArrayList<>();
        for (Inscription inscription : inscriptions) {
            toutesAlertes.addAll(analyserEtudiant(inscription.getEtudiant().getId(), promotionId));
        }

        log.info("Analyse promotion {} : {} alertes générées pour {} étudiants",
                promotionId, toutesAlertes.size(), inscriptions.size());
        return toutesAlertes;
    }

    private boolean alerteExisteDeja(TypeAlerte type, Long etudiantId, Long moduleId) {
        return !alerteRepository.findByTypeAndEtudiantIdAndModuleFormationIdAndStatut(
                type, etudiantId, moduleId, StatutAlerte.ACTIVE).isEmpty();
    }
}
