package com.academiq.repository;

import com.academiq.entity.Etudiant;
import com.academiq.entity.StatutEtudiant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    Optional<Etudiant> findByMatricule(String matricule);

    Optional<Etudiant> findByUtilisateurId(Long utilisateurId);

    Optional<Etudiant> findByUtilisateurEmail(String email);

    boolean existsByMatricule(String matricule);

    List<Etudiant> findByStatut(StatutEtudiant statut);

    List<Etudiant> findByNiveauActuel(String niveau);

    Page<Etudiant> findByNiveauActuelAndStatut(String niveau, StatutEtudiant statut, Pageable pageable);

    @Query("SELECT e FROM Etudiant e JOIN e.utilisateur u " +
            "WHERE LOWER(e.matricule) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Etudiant> rechercher(@Param("keyword") String keyword, Pageable pageable);
}
