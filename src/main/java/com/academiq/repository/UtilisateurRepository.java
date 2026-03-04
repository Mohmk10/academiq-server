package com.academiq.repository;

import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Utilisateur> findByRole(Role role, Pageable pageable);

    long countByRole(Role role);

    long countByActifTrue();

    long countByRoleAndActifTrue(Role role);

    Page<Utilisateur> findByRoleNot(Role role, Pageable pageable);

    @Query("SELECT u FROM Utilisateur u " +
            "WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Utilisateur> rechercher(@Param("keyword") String keyword, Pageable pageable);
}
