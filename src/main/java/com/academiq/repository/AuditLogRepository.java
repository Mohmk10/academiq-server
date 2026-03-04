package com.academiq.repository;

import com.academiq.entity.AuditAction;
import com.academiq.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByDateDesc(Pageable pageable);

    List<AuditLog> findByPerformedByOrderByDateDesc(String performedBy);

    List<AuditLog> findByActionOrderByDateDesc(AuditAction action);

    long countByAction(AuditAction action);
}
