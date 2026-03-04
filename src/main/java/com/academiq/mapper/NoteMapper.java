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
        ModuleFormation module = evaluation.getModuleFormation();
        Promotion promotion = evaluation.getPromotion();
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .nom(evaluation.getNom())
                .type(evaluation.getType() != null ? evaluation.getType().name() : null)
                .statut(evaluation.getStatut() != null ? evaluation.getStatut().name() : null)
                .dateEvaluation(evaluation.getDateEvaluation())
                .noteMaximale(evaluation.getNoteMaximale())
                .coefficient(evaluation.getCoefficient())
                .description(evaluation.getDescription())
                .moduleId(module != null ? module.getId() : null)
                .moduleNom(module != null ? module.getNom() : null)
                .promotionId(promotion != null ? promotion.getId() : null)
                .promotionNom(promotion != null ? promotion.getNom() : null)
                .nombreNotesSaisies(noteRepository.countByEvaluationIdAndValeurIsNotNull(evaluation.getId()))
                .nombreInscrits(promotion != null ? inscriptionRepository.countByPromotionId(promotion.getId()) : 0)
                .moyenne(noteRepository.calculerMoyenneEvaluation(evaluation.getId()))
                .noteMin(noteRepository.findNoteMinByEvaluation(evaluation.getId()))
                .noteMax(noteRepository.findNoteMaxByEvaluation(evaluation.getId()))
                .build();
    }

    public List<EvaluationResponse> toEvaluationResponseList(List<Evaluation> evaluations) {
        return evaluations.stream().map(this::toEvaluationResponse).toList();
    }

    public NoteResponse toNoteResponse(Note note) {
        var etudiant = note.getEtudiant();
        Utilisateur etudiantUser = etudiant != null ? etudiant.getUtilisateur() : null;
        var evaluation = note.getEvaluation();
        return NoteResponse.builder()
                .id(note.getId())
                .valeur(note.getValeur())
                .absent(note.isAbsent())
                .commentaire(note.getCommentaire())
                .etudiantId(etudiant != null ? etudiant.getId() : null)
                .etudiantNom(etudiantUser != null ? etudiantUser.getPrenom() + " " + etudiantUser.getNom() : null)
                .etudiantMatricule(etudiant != null ? etudiant.getMatricule() : null)
                .evaluationId(evaluation != null ? evaluation.getId() : null)
                .evaluationNom(evaluation != null ? evaluation.getNom() : null)
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
