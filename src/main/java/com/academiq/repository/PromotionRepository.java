package com.academiq.repository;

import com.academiq.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByNiveauId(Long niveauId);

    Optional<Promotion> findByNiveauIdAndAnneeUniversitaire(Long niveauId, String anneeUniversitaire);

    boolean existsByNiveauIdAndAnneeUniversitaire(Long niveauId, String anneeUniversitaire);

    List<Promotion> findByActifTrue();

    List<Promotion> findByAnneeUniversitaire(String anneeUniversitaire);
}
