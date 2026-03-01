package com.academiq.repository;

import com.academiq.entity.Semestre;
import com.academiq.entity.SemestreEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SemestreRepository extends JpaRepository<Semestre, Long> {

    List<Semestre> findByNiveauId(Long niveauId);

    Optional<Semestre> findByNiveauIdAndSemestre(Long niveauId, SemestreEnum semestre);

    boolean existsByNiveauIdAndSemestre(Long niveauId, SemestreEnum semestre);
}
