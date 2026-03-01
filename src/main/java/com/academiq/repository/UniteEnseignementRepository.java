package com.academiq.repository;

import com.academiq.entity.UniteEnseignement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UniteEnseignementRepository extends JpaRepository<UniteEnseignement, Long> {

    List<UniteEnseignement> findBySemestreId(Long semestreId);

    Optional<UniteEnseignement> findBySemestreIdAndCode(Long semestreId, String code);

    boolean existsBySemestreIdAndCode(Long semestreId, String code);
}
