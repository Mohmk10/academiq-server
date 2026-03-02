package com.academiq.service;

import com.academiq.dto.note.EvaluationRecapDTO;
import com.academiq.dto.note.ModuleNotesDTO;
import com.academiq.dto.note.NoteDetailDTO;
import com.academiq.dto.note.NoteSaisieDTO;
import com.academiq.dto.note.RecapitulatifEtudiantDTO;
import com.academiq.dto.note.RecapitulatifModuleDTO;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Inscription;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Note;
import com.academiq.entity.Promotion;
import com.academiq.entity.StatutEvaluation;
import com.academiq.entity.StatutInscription;
import com.academiq.entity.TypeEvaluation;
import com.academiq.entity.Utilisateur;
import com.academiq.exception.BadRequestException;
import com.academiq.exception.DuplicateResourceException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.EvaluationRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    private final NoteRepository noteRepository;
    private final EvaluationRepository evaluationRepository;
    private final EtudiantRepository etudiantRepository;
    private final InscriptionRepository inscriptionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ModuleFormationRepository moduleFormationRepository;
    private final PromotionRepository promotionRepository;
    private final NoteValidationService noteValidationService;
    private final HistoriqueNoteService historiqueNoteService;

    // ======================== Évaluations ========================

    public List<Evaluation> getEvaluationsByModule(Long moduleId) {
        return evaluationRepository.findByModuleFormationId(moduleId);
    }

    public List<Evaluation> getEvaluationsByModuleAndPromotion(Long moduleId, Long promotionId) {
        return evaluationRepository.findByModuleFormationIdAndPromotionId(moduleId, promotionId);
    }

    public Evaluation getEvaluationById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation", "id", id));
    }

    @Transactional
    public Evaluation createEvaluation(Evaluation evaluation) {
        moduleFormationRepository.findById(evaluation.getModuleFormation().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", evaluation.getModuleFormation().getId()));
        promotionRepository.findById(evaluation.getPromotion().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", evaluation.getPromotion().getId()));

        if (evaluationRepository.existsByModuleFormationIdAndPromotionIdAndNom(
                evaluation.getModuleFormation().getId(), evaluation.getPromotion().getId(), evaluation.getNom())) {
            throw new DuplicateResourceException("Évaluation", "nom", evaluation.getNom());
        }

        evaluation.setStatut(StatutEvaluation.PLANIFIEE);
        Evaluation saved = evaluationRepository.save(evaluation);
        log.info("Évaluation créée : {} ({})", saved.getNom(), saved.getType());
        return saved;
    }

    @Transactional
    public Evaluation updateEvaluation(Long id, Evaluation data) {
        Evaluation evaluation = getEvaluationById(id);
        noteValidationService.validerEvaluationModifiable(evaluation);

        if (data.getNom() != null) {
            evaluation.setNom(data.getNom());
        }
        if (data.getType() != null) {
            evaluation.setType(data.getType());
        }
        if (data.getDateEvaluation() != null) {
            evaluation.setDateEvaluation(data.getDateEvaluation());
        }
        if (data.getDescription() != null) {
            evaluation.setDescription(data.getDescription());
        }
        if (data.getNoteMaximale() > 0) {
            evaluation.setNoteMaximale(data.getNoteMaximale());
        }
        if (data.getCoefficient() > 0) {
            evaluation.setCoefficient(data.getCoefficient());
        }

        return evaluationRepository.save(evaluation);
    }

    @Transactional
    public void deleteEvaluation(Long id) {
        Evaluation evaluation = getEvaluationById(id);
        if (evaluation.getStatut() != StatutEvaluation.PLANIFIEE) {
            throw new BadRequestException("Impossible de supprimer une évaluation avec des notes");
        }
        evaluationRepository.delete(evaluation);
        log.info("Évaluation supprimée : {}", id);
    }

    // ======================== Saisie des Notes ========================

    @Transactional
    public Note saisirNote(Long evaluationId, Long etudiantId, Double valeur, boolean absent,
                           String commentaire, Long saisiParId) {
        Evaluation evaluation = getEvaluationById(evaluationId);
        noteValidationService.validerEvaluationModifiable(evaluation);

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));

        noteValidationService.validerEtudiantInscrit(etudiantId, evaluation.getPromotion().getId(), inscriptionRepository);
        noteValidationService.validerNote(valeur, evaluation.getNoteMaximale(), absent);

        if (absent) {
            valeur = null;
        }

        Utilisateur saisiPar = null;
        if (saisiParId != null) {
            saisiPar = utilisateurRepository.findById(saisiParId).orElse(null);
        }

        Note note;
        var existingNote = noteRepository.findByEtudiantIdAndEvaluationId(etudiantId, evaluationId);

        if (existingNote.isPresent()) {
            note = existingNote.get();
            historiqueNoteService.enregistrerModification(
                    note, note.getValeur(), note.isAbsent(), saisiParId, commentaire);
            note.setAncienneValeur(note.getValeur());
            note.setValeur(valeur);
            note.setAbsent(absent);
            note.setCommentaire(commentaire);
            note.setSaisiePar(saisiPar);
            note.setDateModification(LocalDateTime.now());
        } else {
            note = Note.builder()
                    .etudiant(etudiant)
                    .evaluation(evaluation)
                    .valeur(valeur)
                    .absent(absent)
                    .commentaire(commentaire)
                    .saisiePar(saisiPar)
                    .dateSaisie(LocalDateTime.now())
                    .build();
        }

        Note saved = noteRepository.save(note);

        if (evaluation.getStatut() == StatutEvaluation.PLANIFIEE) {
            evaluation.setStatut(StatutEvaluation.EN_COURS);
            evaluationRepository.save(evaluation);
        }

        log.info("Note saisie pour étudiant {} - évaluation {} : {}", etudiantId, evaluationId, valeur);
        return saved;
    }

    @Transactional
    public List<Note> saisirNotesEnMasse(Long evaluationId, List<NoteSaisieDTO> notesDtos, Long saisiParId) {
        List<Note> notes = new ArrayList<>();
        for (NoteSaisieDTO dto : notesDtos) {
            Note note = saisirNote(evaluationId, dto.getEtudiantId(), dto.getValeur(),
                    dto.isAbsent(), dto.getCommentaire(), saisiParId);
            notes.add(note);
        }
        return notes;
    }

    @Transactional
    public void terminerSaisie(Long evaluationId) {
        Evaluation evaluation = getEvaluationById(evaluationId);
        evaluation.setStatut(StatutEvaluation.TERMINEE);
        evaluationRepository.save(evaluation);
        log.info("Saisie terminée pour évaluation {}", evaluationId);
    }

    // ======================== Verrouillage ========================

    @Transactional
    public void verrouillerEvaluation(Long evaluationId) {
        Evaluation evaluation = getEvaluationById(evaluationId);
        if (evaluation.getStatut() != StatutEvaluation.TERMINEE) {
            throw new BadRequestException("La saisie doit être terminée avant le verrouillage");
        }
        evaluation.setStatut(StatutEvaluation.VERROUILLEE);
        evaluationRepository.save(evaluation);
        log.info("Évaluation {} verrouillée", evaluationId);
    }

    @Transactional
    public void deverrouillerEvaluation(Long evaluationId) {
        Evaluation evaluation = getEvaluationById(evaluationId);
        if (evaluation.getStatut() != StatutEvaluation.VERROUILLEE) {
            throw new BadRequestException("L'évaluation n'est pas verrouillée");
        }
        evaluation.setStatut(StatutEvaluation.TERMINEE);
        evaluationRepository.save(evaluation);
        log.info("Évaluation {} déverrouillée", evaluationId);
    }

    // ======================== Consultation ========================

    public List<Note> getNotesByEvaluation(Long evaluationId) {
        return noteRepository.findByEvaluationId(evaluationId);
    }

    public List<Note> getNotesByEtudiant(Long etudiantId) {
        return noteRepository.findByEtudiantId(etudiantId);
    }

    public List<Note> getNotesByEtudiantAndModule(Long etudiantId, Long moduleId) {
        return noteRepository.findByEtudiantIdAndModuleId(etudiantId, moduleId);
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));
    }

    // ======================== Récapitulatifs ========================

    public RecapitulatifModuleDTO getRecapitulatifModule(Long moduleId, Long promotionId) {
        ModuleFormation module = moduleFormationRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        List<Evaluation> evaluations = evaluationRepository
                .findByModuleFormationIdAndPromotionId(moduleId, promotionId);

        double sommeCoeffMoyenne = 0;
        double sommeCoeff = 0;

        List<EvaluationRecapDTO> recaps = new ArrayList<>();
        for (Evaluation eval : evaluations) {
            Double moyenne = noteRepository.calculerMoyenneEvaluation(eval.getId());
            Double noteMin = noteRepository.findNoteMinByEvaluation(eval.getId());
            Double noteMax = noteRepository.findNoteMaxByEvaluation(eval.getId());
            long nombreNotes = noteRepository.countByEvaluationIdAndValeurIsNotNull(eval.getId());
            long nombreAbsents = noteRepository.countByEvaluationIdAndAbsentTrue(eval.getId());

            recaps.add(EvaluationRecapDTO.builder()
                    .evaluationId(eval.getId())
                    .evaluationNom(eval.getNom())
                    .type(eval.getType().name())
                    .coefficient(eval.getCoefficient())
                    .noteMaximale(eval.getNoteMaximale())
                    .moyenne(moyenne)
                    .noteMin(noteMin)
                    .noteMax(noteMax)
                    .nombreNotes(nombreNotes)
                    .nombreAbsents(nombreAbsents)
                    .build());

            if (moyenne != null) {
                sommeCoeffMoyenne += moyenne * eval.getCoefficient();
                sommeCoeff += eval.getCoefficient();
            }
        }

        Double moyenneClasse = sommeCoeff > 0 ? sommeCoeffMoyenne / sommeCoeff : null;

        return RecapitulatifModuleDTO.builder()
                .moduleId(module.getId())
                .moduleNom(module.getNom())
                .moduleCode(module.getCode())
                .evaluations(recaps)
                .moyenneClasse(moyenneClasse)
                .build();
    }

    public RecapitulatifEtudiantDTO getRecapitulatifEtudiant(Long etudiantId, Long promotionId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        Inscription inscription = inscriptionRepository.findByEtudiantIdAndPromotionId(etudiantId, promotionId)
                .orElseThrow(() -> new BadRequestException("L'étudiant n'est pas inscrit à cette promotion"));

        List<Note> notes = noteRepository.findByEtudiantIdAndPromotionId(etudiantId, promotionId);

        Set<TypeEvaluation> typesCC = EnumSet.of(
                TypeEvaluation.CC, TypeEvaluation.TP, TypeEvaluation.PROJET);

        Map<Long, List<Note>> notesByModule = notes.stream()
                .collect(Collectors.groupingBy(n -> n.getEvaluation().getModuleFormation().getId()));

        List<ModuleNotesDTO> modulesDTO = new ArrayList<>();

        for (Map.Entry<Long, List<Note>> entry : notesByModule.entrySet()) {
            List<Note> notesModule = entry.getValue();
            ModuleFormation module = notesModule.get(0).getEvaluation().getModuleFormation();

            List<NoteDetailDTO> noteDetails = notesModule.stream()
                    .map(n -> NoteDetailDTO.builder()
                            .noteId(n.getId())
                            .evaluationNom(n.getEvaluation().getNom())
                            .type(n.getEvaluation().getType().name())
                            .valeur(n.getValeur())
                            .absent(n.isAbsent())
                            .noteMaximale(n.getEvaluation().getNoteMaximale())
                            .coefficient(n.getEvaluation().getCoefficient())
                            .build())
                    .toList();

            Double moyenneModule = calculerMoyenneModule(notesModule, module, typesCC);

            modulesDTO.add(ModuleNotesDTO.builder()
                    .moduleId(module.getId())
                    .moduleNom(module.getNom())
                    .moduleCode(module.getCode())
                    .ueNom(module.getUniteEnseignement().getNom())
                    .coefficient(module.getCoefficient())
                    .credits(module.getCredits())
                    .notes(noteDetails)
                    .moyenneModule(moyenneModule)
                    .build());
        }

        var utilisateur = etudiant.getUtilisateur();
        return RecapitulatifEtudiantDTO.builder()
                .etudiantId(etudiant.getId())
                .etudiantNom(utilisateur.getPrenom() + " " + utilisateur.getNom())
                .etudiantMatricule(etudiant.getMatricule())
                .promotionNom(promotion.getNom())
                .modules(modulesDTO)
                .build();
    }

    private Double calculerMoyenneModule(List<Note> notes, ModuleFormation module,
                                         Set<TypeEvaluation> typesCC) {
        List<Note> notesCC = notes.stream()
                .filter(n -> typesCC.contains(n.getEvaluation().getType()))
                .filter(n -> !n.isAbsent() && n.getValeur() != null)
                .toList();

        List<Note> notesExamen = notes.stream()
                .filter(n -> n.getEvaluation().getType() == TypeEvaluation.EXAMEN
                        || n.getEvaluation().getType() == TypeEvaluation.PARTIEL)
                .filter(n -> !n.isAbsent() && n.getValeur() != null)
                .toList();

        Double moyenneCC = null;
        if (!notesCC.isEmpty()) {
            double sommeCoeffValeur = 0;
            double sommeCoeff = 0;
            for (Note n : notesCC) {
                double noteRamenee = (n.getValeur() / n.getEvaluation().getNoteMaximale()) * 20;
                sommeCoeffValeur += noteRamenee * n.getEvaluation().getCoefficient();
                sommeCoeff += n.getEvaluation().getCoefficient();
            }
            moyenneCC = sommeCoeff > 0 ? sommeCoeffValeur / sommeCoeff : null;
        }

        Double noteExamen = null;
        if (!notesExamen.isEmpty()) {
            Note derniere = notesExamen.get(notesExamen.size() - 1);
            noteExamen = (derniere.getValeur() / derniere.getEvaluation().getNoteMaximale()) * 20;
        }

        if (moyenneCC != null && noteExamen != null) {
            return moyenneCC * module.getPonderationCC() + noteExamen * module.getPonderationExamen();
        } else if (moyenneCC != null) {
            return moyenneCC;
        } else if (noteExamen != null) {
            return noteExamen;
        }
        return null;
    }
}
