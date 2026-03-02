package com.academiq.mapper;

import com.academiq.dto.note.EvaluationRequest;
import com.academiq.dto.note.EvaluationResponse;
import com.academiq.dto.note.NoteResponse;
import com.academiq.entity.Evaluation;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Note;
import com.academiq.entity.Promotion;
import com.academiq.entity.Utilisateur;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NoteMapper {

    private final NoteRepository noteRepository;
    private final InscriptionRepository inscriptionRepository;

    public Evaluation toEvaluation(EvaluationRequest request) {
        ModuleFormation module = new ModuleFormation();
        module.setId(request.getModuleFormationId());

        Promotion promotion = new Promotion();
        promotion.setId(request.getPromotionId());

        return Evaluation.builder()
                .nom(request.getNom())
                .type(request.getType())
                .dateEvaluation(request.getDateEvaluation())
                .noteMaximale(request.getNoteMaximale())
                .coefficient(request.getCoefficient())
                .description(request.getDescription())
                .moduleFormation(module)
                .promotion(promotion)
                .build();
    }

    public EvaluationResponse toEvaluationResponse(Evaluation evaluation) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .nom(evaluation.getNom())
                .type(evaluation.getType().name())
                .statut(evaluation.getStatut().name())
                .dateEvaluation(evaluation.getDateEvaluation())
                .noteMaximale(evaluation.getNoteMaximale())
                .coefficient(evaluation.getCoefficient())
                .description(evaluation.getDescription())
                .moduleId(evaluation.getModuleFormation().getId())
                .moduleNom(evaluation.getModuleFormation().getNom())
                .promotionId(evaluation.getPromotion().getId())
                .promotionNom(evaluation.getPromotion().getNom())
                .nombreNotesSaisies(noteRepository.countByEvaluationIdAndValeurIsNotNull(evaluation.getId()))
                .nombreInscrits(inscriptionRepository.countByPromotionId(evaluation.getPromotion().getId()))
                .moyenne(noteRepository.calculerMoyenneEvaluation(evaluation.getId()))
                .noteMin(noteRepository.findNoteMinByEvaluation(evaluation.getId()))
                .noteMax(noteRepository.findNoteMaxByEvaluation(evaluation.getId()))
                .build();
    }

    public List<EvaluationResponse> toEvaluationResponseList(List<Evaluation> evaluations) {
        return evaluations.stream().map(this::toEvaluationResponse).toList();
    }

    public NoteResponse toNoteResponse(Note note) {
        Utilisateur etudiantUser = note.getEtudiant().getUtilisateur();
        return NoteResponse.builder()
                .id(note.getId())
                .valeur(note.getValeur())
                .absent(note.isAbsent())
                .commentaire(note.getCommentaire())
                .etudiantId(note.getEtudiant().getId())
                .etudiantNom(etudiantUser.getPrenom() + " " + etudiantUser.getNom())
                .etudiantMatricule(note.getEtudiant().getMatricule())
                .evaluationId(note.getEvaluation().getId())
                .evaluationNom(note.getEvaluation().getNom())
                .saisiePar(note.getSaisiePar() != null
                        ? note.getSaisiePar().getPrenom() + " " + note.getSaisiePar().getNom()
                        : null)
                .dateSaisie(note.getDateSaisie())
                .dateModification(note.getDateModification())
                .ancienneValeur(note.getAncienneValeur())
                .build();
    }

    public List<NoteResponse> toNoteResponseList(List<Note> notes) {
        return notes.stream().map(this::toNoteResponse).toList();
    }
}
