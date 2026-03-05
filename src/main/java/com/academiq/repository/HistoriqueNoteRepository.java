package com.academiq.repository;

import com.academiq.entity.HistoriqueNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueNoteRepository extends JpaRepository<HistoriqueNote, Long> {

    List<HistoriqueNote> findByNoteIdOrderByDateModificationDesc(Long noteId);

    List<HistoriqueNote> findByModifieParIdOrderByDateModificationDesc(Long utilisateurId);

    void deleteByNoteId(Long noteId);
}
