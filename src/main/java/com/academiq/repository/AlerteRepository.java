package com.academiq.repository;

import com.academiq.entity.Alerte;
import com.academiq.entity.NiveauAlerte;
import com.academiq.entity.StatutAlerte;
import com.academiq.entity.TypeAlerte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByEtudiantId(Long etudiantId);

    List<Alerte> findByEtudiantIdAndStatut(Long etudiantId, StatutAlerte statut);

    List<Alerte> findByPromotionId(Long promotionId);

    List<Alerte> findByPromotionIdAndStatut(Long promotionId, StatutAlerte statut);

    List<Alerte> findByStatut(StatutAlerte statut);

    List<Alerte> findByNiveau(NiveauAlerte niveau);

    List<Alerte> findByTypeAndEtudiantIdAndModuleFormationIdAndStatut(
            TypeAlerte type, Long etudiantId, Long moduleId, StatutAlerte statut);

    long countByStatut(StatutAlerte statut);

    long countByNiveauAndStatut(NiveauAlerte niveau, StatutAlerte statut);

    long countByTypeAndStatut(TypeAlerte type, StatutAlerte statut);

    Page<Alerte> findByStatutAndNiveau(StatutAlerte statut, NiveauAlerte niveau, Pageable pageable);

    @Query("SELECT a FROM Alerte a WHERE " +
            "(:statut IS NULL OR a.statut = :statut) AND " +
            "(:niveau IS NULL OR a.niveau = :niveau) AND " +
            "(:type IS NULL OR a.type = :type)")
    Page<Alerte> rechercherAlertes(@Param("statut") StatutAlerte statut,
                                   @Param("niveau") NiveauAlerte niveau,
                                   @Param("type") TypeAlerte type,
                                   Pageable pageable);
}
