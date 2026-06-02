package com.pairflow.audit;

import com.pairflow.config.CurrentUser;
import com.pairflow.couple.CoupleContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-log")
public class AuditController {

    private final AuditService auditService;
    private final CoupleContext coupleContext;

    public AuditController(AuditService auditService, CoupleContext coupleContext) {
        this.auditService = auditService;
        this.coupleContext = coupleContext;
    }

    /** Personal audit trail — only the authenticated user's own events. */
    @GetMapping("/me")
    public List<Map<String, Object>> myLog() {
        return auditService.myLog(CurrentUser.id()).stream()
                .map(this::toMap)
                .toList();
    }

    /** All audit events scoped to the couple (both members' actions). */
    @GetMapping("/couple")
    public List<Map<String, Object>> coupleLog() {
        String coupleId = coupleContext.requireCoupleId();
        return auditService.coupleLog(coupleId).stream()
                .map(this::toMap)
                .toList();
    }

    private Map<String, Object> toMap(AuditLog a) {
        return Map.of(
                "id", a.getId(),
                "actorId", a.getActorId(),
                "action", a.getAction().name(),
                "targetType", a.getTargetType() != null ? a.getTargetType() : "",
                "targetId", a.getTargetId() != null ? a.getTargetId() : "",
                "createdAt", a.getCreatedAt()
        );
    }
}
