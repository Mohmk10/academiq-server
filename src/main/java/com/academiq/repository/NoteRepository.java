package com.academiq.repository;

import com.academiq.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByEtudiantIdAndEvaluationId(Long etudiantId, Long evaluationId);

    List<Note> findByEvaluationId(Long evaluationId);

    List<Note> findByEtudiantId(Long etudiantId);

    boolean existsByEtudiantIdAndEvaluationId(Long etudiantId, Long evaluationId);

    long countByEvaluationId(Long evaluationId);

    long countByEvaluationIdAndValeurIsNotNull(Long evaluationId);

    long countByEvaluationIdAndAbsentTrue(Long evaluationId);

    @Query("SELECT n FROM Note n WHERE n.etudiant.id = :etudiantId " +
            "AND n.evaluation.moduleFormation.id = :moduleId")
    List<Note> findByEtudiantIdAndModuleId(@Param("etudiantId") Long etudiantId,
                                           @Param("moduleId") Long moduleId);

    @Query("SELECT n FROM Note n WHERE n.etudiant.id = :etudiantId " +
            "AND n.evaluation.promotion.id = :promotionId")
    List<Note> findByEtudiantIdAndPromotionId(@Param("etudiantId") Long etudiantId,
                                              @Param("promotionId") Long promotionId);

    @Query("SELECT AVG(n.valeur) FROM Note n WHERE n.evaluation.id = :evaluationId " +
            "AND n.valeur IS NOT NULL AND n.absent = false")
    Double calculerMoyenneEvaluation(@Param("evaluationId") Long evaluationId);

    @Query("SELECT MIN(n.valeur) FROM Note n WHERE n.evaluation.id = :evaluationId " +
            "AND n.valeur IS NOT NULL AND n.absent = false")
    Double findNoteMinByEvaluation(@Param("evaluationId") Long evaluationId);

    @Query("SELECT MAX(n.valeur) FROM Note n WHERE n.evaluation.id = :evaluationId " +
            "AND n.valeur IS NOT NULL AND n.absent = false")
    Double findNoteMaxByEvaluation(@Param("evaluationId") Long evaluationId);
}
