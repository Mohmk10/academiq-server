package com.academiq.repository;

import com.academiq.entity.RegleAlerte;
import com.academiq.entity.TypeAlerte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegleAlerteRepository extends JpaRepository<RegleAlerte, Long> {

    List<RegleAlerte> findByActifTrue();

    List<RegleAlerte> findByType(TypeAlerte type);

    Optional<RegleAlerte> findByNom(String nom);

    boolean existsByNom(String nom);
}
