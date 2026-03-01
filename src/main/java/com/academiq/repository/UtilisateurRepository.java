package com.academiq.repository;

import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Utilisateur> findByRole(Role role, Pageable pageable);

    long countByRole(Role role);

    long countByActifTrue();
}
