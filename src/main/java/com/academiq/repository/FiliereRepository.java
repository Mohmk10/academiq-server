package com.academiq.repository;

import com.academiq.entity.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FiliereRepository extends JpaRepository<Filiere, Long> {

    Optional<Filiere> findByCode(String code);

    Optional<Filiere> findByNom(String nom);

    boolean existsByCode(String code);

    boolean existsByNom(String nom);

    List<Filiere> findByActifTrue();
}
