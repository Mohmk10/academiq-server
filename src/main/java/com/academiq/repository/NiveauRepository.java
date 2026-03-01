package com.academiq.repository;

import com.academiq.entity.Niveau;
import com.academiq.entity.NiveauEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NiveauRepository extends JpaRepository<Niveau, Long> {

    List<Niveau> findByFiliereId(Long filiereId);

    Optional<Niveau> findByFiliereIdAndNiveau(Long filiereId, NiveauEnum niveau);

    boolean existsByFiliereIdAndNiveau(Long filiereId, NiveauEnum niveau);
}
