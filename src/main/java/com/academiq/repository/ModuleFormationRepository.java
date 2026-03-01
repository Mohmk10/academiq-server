package com.academiq.repository;

import com.academiq.entity.ModuleFormation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuleFormationRepository extends JpaRepository<ModuleFormation, Long> {

    List<ModuleFormation> findByUniteEnseignementId(Long ueId);

    Optional<ModuleFormation> findByUniteEnseignementIdAndCode(Long ueId, String code);

    boolean existsByUniteEnseignementIdAndCode(Long ueId, String code);

    List<ModuleFormation> findByEnseignantId(Long enseignantId);

    Optional<ModuleFormation> findByCode(String code);
}
