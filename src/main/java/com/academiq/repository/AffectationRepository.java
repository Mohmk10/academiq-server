package com.academiq.repository;

import com.academiq.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AffectationRepository extends JpaRepository<Affectation, Long> {

    List<Affectation> findByEnseignantId(Long enseignantId);

    List<Affectation> findByModuleFormationId(Long moduleId);

    List<Affectation> findByPromotionId(Long promotionId);

    Optional<Affectation> findByEnseignantIdAndModuleFormationIdAndPromotionId(Long enseignantId, Long moduleId, Long promotionId);

    boolean existsByEnseignantIdAndModuleFormationIdAndPromotionId(Long enseignantId, Long moduleId, Long promotionId);

    List<Affectation> findByEnseignantIdAndActifTrue(Long enseignantId);
}
