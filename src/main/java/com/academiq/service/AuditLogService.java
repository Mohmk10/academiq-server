package com.academiq.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    /**
     * Log une action sensible
     */
    public void logAction(String action, String performedBy, String targetUser, String details) {
        log.info("[AUDIT] Action={} | Par={} | Cible={} | Détails={}",
                action, performedBy, targetUser, details);
    }

    public void logRoleChange(String performedBy, String targetUser, String oldRole, String newRole) {
        logAction("CHANGEMENT_ROLE", performedBy, targetUser,
                String.format("Ancien=%s, Nouveau=%s", oldRole, newRole));
    }

    public void logAccountDeletion(String performedBy, String targetUser) {
        logAction("SUPPRESSION_COMPTE", performedBy, targetUser, "Suppression définitive");
    }

    public void logAccountToggle(String performedBy, String targetUser, boolean activated) {
        logAction("TOGGLE_COMPTE", performedBy, targetUser,
                activated ? "Activation" : "Désactivation");
    }

    public void logLogin(String email, boolean success) {
        logAction("CONNEXION", email, email, success ? "Succès" : "Échec");
    }
}
