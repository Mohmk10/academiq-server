package com.academiq.repository;

import com.academiq.entity.Enseignant;
import com.academiq.entity.StatutEnseignant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {

    Optional<Enseignant> findByMatricule(String matricule);

    Optional<Enseignant> findByUtilisateurId(Long utilisateurId);

    Optional<Enseignant> findByUtilisateurEmail(String email);

    boolean existsByMatricule(String matricule);

    List<Enseignant> findByStatut(StatutEnseignant statut);

    List<Enseignant> findBySpecialite(String specialite);

    @Query("SELECT e FROM Enseignant e JOIN e.utilisateur u " +
            "WHERE LOWER(e.matricule) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.specialite) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Enseignant> rechercher(@Param("keyword") String keyword, Pageable pageable);
}
