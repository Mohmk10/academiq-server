package com.academiq.repository;

import com.academiq.entity.Inscription;
import com.academiq.entity.StatutInscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findByEtudiantId(Long etudiantId);

    List<Inscription> findByPromotionId(Long promotionId);

    Optional<Inscription> findByEtudiantIdAndPromotionId(Long etudiantId, Long promotionId);

    boolean existsByEtudiantIdAndPromotionId(Long etudiantId, Long promotionId);

    List<Inscription> findByPromotionIdAndStatut(Long promotionId, StatutInscription statut);

    long countByPromotionId(Long promotionId);
}
