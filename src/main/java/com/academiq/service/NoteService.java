package com.academiq.service;

import com.academiq.dto.note.NoteSaisieDTO;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Note;
import com.academiq.entity.StatutEvaluation;
import com.academiq.entity.StatutInscription;
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
import java.util.List;

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
        if (evaluation.getStatut() == StatutEvaluation.VERROUILLEE) {
            throw new BadRequestException("Cette évaluation est verrouillée, aucune modification possible");
        }

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
        if (evaluation.getStatut() == StatutEvaluation.VERROUILLEE) {
            throw new BadRequestException("Cette évaluation est verrouillée, aucune modification possible");
        }

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));

        boolean inscrit = inscriptionRepository.findByEtudiantIdAndPromotionId(etudiantId, evaluation.getPromotion().getId())
                .filter(i -> i.getStatut() == StatutInscription.ACTIVE)
                .isPresent();
        if (!inscrit) {
            throw new BadRequestException("L'étudiant n'est pas inscrit à cette promotion");
        }

        if (absent) {
            valeur = null;
        } else {
            if (valeur == null) {
                throw new BadRequestException("La note est obligatoire pour un étudiant présent");
            }
            if (valeur < 0) {
                throw new BadRequestException("La note ne peut pas être négative");
            }
            if (valeur > evaluation.getNoteMaximale()) {
                throw new BadRequestException("La note ne peut pas dépasser " + evaluation.getNoteMaximale());
            }
        }

        Utilisateur saisiPar = null;
        if (saisiParId != null) {
            saisiPar = utilisateurRepository.findById(saisiParId).orElse(null);
        }

        Note note;
        var existingNote = noteRepository.findByEtudiantIdAndEvaluationId(etudiantId, evaluationId);

        if (existingNote.isPresent()) {
            note = existingNote.get();
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
}
