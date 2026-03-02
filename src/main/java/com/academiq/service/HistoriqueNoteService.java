package com.academiq.service;

import com.academiq.entity.HistoriqueNote;
import com.academiq.entity.Note;
import com.academiq.entity.Utilisateur;
import com.academiq.repository.HistoriqueNoteRepository;
import com.academiq.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriqueNoteService {

    private final HistoriqueNoteRepository historiqueNoteRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public void enregistrerModification(Note note, Double ancienneValeur, boolean ancienAbsent,
                                        Long modifieParId, String motif) {
        Utilisateur modifiePar = null;
        if (modifieParId != null) {
            modifiePar = utilisateurRepository.findById(modifieParId).orElse(null);
        }

        HistoriqueNote historique = HistoriqueNote.builder()
                .note(note)
                .ancienneValeur(ancienneValeur)
                .nouvelleValeur(note.getValeur())
                .ancienAbsent(ancienAbsent)
                .nouveauAbsent(note.isAbsent())
                .motifModification(motif)
                .modifiePar(modifiePar)
                .dateModification(LocalDateTime.now())
                .build();

        historiqueNoteRepository.save(historique);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueNote> getHistoriqueByNote(Long noteId) {
        return historiqueNoteRepository.findByNoteIdOrderByDateModificationDesc(noteId);
    }
}
