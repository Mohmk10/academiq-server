package com.academiq.service;

import com.academiq.dto.note.NotePrepopuleeDTO;
import com.academiq.dto.note.NoteSaisieDTO;
import com.academiq.dto.note.SaisieEnMasseResult;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Inscription;
import com.academiq.entity.Note;
import com.academiq.entity.StatutInscription;
import com.academiq.exception.BadRequestException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SaisieEnMasseService {

    private static final Logger log = LoggerFactory.getLogger(SaisieEnMasseService.class);

    private final NoteService noteService;
    private final EvaluationRepository evaluationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public SaisieEnMasseResult saisirNotesClasse(Long evaluationId, List<NoteSaisieDTO> notes, Long saisiParId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation", "id", evaluationId));

        if (evaluation.getStatut() == com.academiq.entity.StatutEvaluation.VERROUILLEE) {
            throw new BadRequestException("L'évaluation est verrouillée, modification impossible");
        }

        int totalSucces = 0;
        int totalErreurs = 0;
        List<String> erreurs = new ArrayList<>();

        for (NoteSaisieDTO dto : notes) {
            try {
                noteService.saisirNote(evaluationId, dto.getEtudiantId(), dto.getValeur(),
                        dto.isAbsent(), dto.getCommentaire(), saisiParId);
                totalSucces++;
            } catch (Exception e) {
                totalErreurs++;
                erreurs.add("Étudiant " + dto.getEtudiantId() + " : " + e.getMessage());
                log.warn("Erreur saisie note étudiant {} : {}", dto.getEtudiantId(), e.getMessage());
            }
        }

        log.info("Saisie en masse évaluation {} : {}/{} succès", evaluationId, totalSucces, notes.size());

        return SaisieEnMasseResult.builder()
                .totalTraites(notes.size())
                .totalSucces(totalSucces)
                .totalErreurs(totalErreurs)
                .erreurs(erreurs)
                .build();
    }

    @Transactional(readOnly = true)
    public List<NotePrepopuleeDTO> preparerSaisie(Long evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation", "id", evaluationId));

        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(evaluation.getPromotion().getId(), StatutInscription.ACTIVE);

        List<NotePrepopuleeDTO> result = new ArrayList<>();

        for (Inscription inscription : inscriptions) {
            var etudiant = inscription.getEtudiant();
            var utilisateur = etudiant.getUtilisateur();

            Optional<Note> noteExistante = noteRepository
                    .findByEtudiantIdAndEvaluationId(etudiant.getId(), evaluationId);

            NotePrepopuleeDTO dto = NotePrepopuleeDTO.builder()
                    .etudiantId(etudiant.getId())
                    .etudiantNom(utilisateur.getPrenom() + " " + utilisateur.getNom())
                    .etudiantMatricule(etudiant.getMatricule())
                    .noteExistante(noteExistante.map(Note::getValeur).orElse(null))
                    .absent(noteExistante.map(Note::isAbsent).orElse(false))
                    .dejaNotee(noteExistante.isPresent())
                    .build();

            result.add(dto);
        }

        return result;
    }
}
