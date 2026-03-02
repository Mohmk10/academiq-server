package com.academiq.repository;

import com.academiq.entity.Evaluation;
import com.academiq.entity.StatutEvaluation;
import com.academiq.entity.TypeEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByModuleFormationId(Long moduleId);

    List<Evaluation> findByPromotionId(Long promotionId);

    List<Evaluation> findByModuleFormationIdAndPromotionId(Long moduleId, Long promotionId);

    List<Evaluation> findByModuleFormationIdAndType(Long moduleId, TypeEvaluation type);

    List<Evaluation> findByModuleFormationIdAndPromotionIdAndType(Long moduleId, Long promotionId, TypeEvaluation type);

    List<Evaluation> findByStatut(StatutEvaluation statut);

    boolean existsByModuleFormationIdAndPromotionIdAndNom(Long moduleId, Long promotionId, String nom);
}
