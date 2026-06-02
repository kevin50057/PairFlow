package com.pairflow.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AuditService {

    private final AuditRepository repository;

    public AuditService(AuditRepository repository) {
        this.repository = repository;
    }

    /**
     * Records an audit event. Uses REQUIRES_NEW so a rollback in the calling
     * transaction never silently drops the audit record.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actorId, String coupleId, AuditAction action,
                    String targetType, String targetId, String ipAddress) {
        try {
            AuditLog entry = new AuditLog();
            entry.setActorId(actorId);
            entry.setCoupleId(coupleId);
            entry.setAction(action);
            entry.setTargetType(targetType);
            entry.setTargetId(targetId);
            entry.setIpAddress(ipAddress);
            repository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to write audit log [{} / {}]: {}", actorId, action, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> myLog(String actorId) {
        return repository.findByActorIdOrderByCreatedAtDesc(actorId, PageRequest.of(0, 200));
    }

    @Transactional(readOnly = true)
    public List<AuditLog> coupleLog(String coupleId) {
        return repository.findByCoupleIdOrderByCreatedAtDesc(coupleId, PageRequest.of(0, 200));
    }
}
