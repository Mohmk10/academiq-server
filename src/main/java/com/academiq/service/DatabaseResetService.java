package com.academiq.service;

import com.academiq.config.DataInitializer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseResetService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseResetService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final DataInitializer dataInitializer;

    private static final String[] TABLES = {
            "historique_notes", "notes", "evaluations", "alertes", "regles_alerte",
            "audit_logs", "affectations", "inscriptions", "etudiants", "enseignants",
            "admins", "modules_formation", "ues", "semestres", "promotions",
            "niveaux", "filieres", "utilisateurs"
    };

    @Transactional
    public void resetDatabase() {
        log.warn("=== RESET COMPLET DE LA BASE DE DONNÉES ===");

        for (String table : TABLES) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + table + " CASCADE").executeUpdate();
            log.info("Table {} vidée", table);
        }

        for (String table : TABLES) {
            try {
                entityManager.createNativeQuery(
                        "ALTER SEQUENCE " + table + "_id_seq RESTART WITH 1").executeUpdate();
                log.info("Séquence {}_id_seq réinitialisée", table);
            } catch (Exception e) {
                log.debug("Pas de séquence {}_id_seq (normal pour certaines tables)", table);
            }
        }

        entityManager.flush();
        entityManager.clear();

        try {
            dataInitializer.run();
            log.info("Données initiales recréées (SUPER_ADMIN ID=1)");
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation des données : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la recréation des données initiales", e);
        }

        log.warn("=== RESET TERMINÉ ===");
    }
}
