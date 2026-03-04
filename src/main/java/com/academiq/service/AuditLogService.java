package com.academiq.service;

import com.academiq.entity.AuditAction;
import com.academiq.entity.AuditLog;
import com.academiq.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(AuditAction action, String performedBy, String targetUser, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .performedBy(performedBy)
                    .targetUser(targetUser)
                    .details(details)
                    .date(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de l'audit log", e);
        }
        log.info("[AUDIT] Action={} | Par={} | Cible={} | Détails={}",
                action, performedBy, targetUser, details);
    }

    @Transactional
    public void logRoleChange(String performedBy, String targetUser, String oldRole, String newRole) {
        logAction(AuditAction.CHANGEMENT_ROLE, performedBy, targetUser,
                String.format("Ancien=%s, Nouveau=%s", oldRole, newRole));
    }

    @Transactional
    public void logAccountDeletion(String performedBy, String targetUser) {
        logAction(AuditAction.SUPPRESSION_UTILISATEUR, performedBy, targetUser, "Suppression définitive");
    }

    @Transactional
    public void logAccountToggle(String performedBy, String targetUser, boolean activated) {
        logAction(activated ? AuditAction.ACTIVATION_COMPTE : AuditAction.DESACTIVATION_COMPTE,
                performedBy, targetUser, activated ? "Activation" : "Désactivation");
    }

    @Transactional
    public void logLogin(String email, boolean success) {
        logAction(AuditAction.CONNEXION, email, email, success ? "Succès" : "Échec");
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs(int limit) {
        Page<AuditLog> page = auditLogRepository.findAllByOrderByDateDesc(PageRequest.of(0, limit));
        return page.getContent();
    }
}
