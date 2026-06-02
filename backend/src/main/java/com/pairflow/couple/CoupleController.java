package com.pairflow.couple;

import com.pairflow.audit.AuditAction;
import com.pairflow.audit.AuditService;
import com.pairflow.config.CurrentUser;
import com.pairflow.couple.dto.BreakupPendingResponse;
import com.pairflow.couple.dto.CoupleResponse;
import com.pairflow.couple.dto.CreateInviteResponse;
import com.pairflow.couple.dto.InitiateBreakupRequest;
import com.pairflow.couple.dto.JoinRequest;
import com.pairflow.couple.dto.UpdateCoupleRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/couples")
public class CoupleController {

    private final CoupleService coupleService;
    private final ExportService exportService;
    private final AuditService auditService;

    public CoupleController(CoupleService coupleService,
                            ExportService exportService,
                            AuditService auditService) {
        this.coupleService = coupleService;
        this.exportService = exportService;
        this.auditService = auditService;
    }

    @PostMapping("/invite")
    public CreateInviteResponse invite() {
        return coupleService.createInvite(CurrentUser.id());
    }

    @PostMapping("/join")
    public CoupleResponse join(@Valid @RequestBody JoinRequest req) {
        return coupleService.join(CurrentUser.id(), req.code());
    }

    @GetMapping("/me")
    public CoupleResponse me() {
        return coupleService.getMyCouple(CurrentUser.id());
    }

    @PatchMapping("/{coupleId}")
    public CoupleResponse update(@PathVariable String coupleId,
                                 @Valid @RequestBody UpdateCoupleRequest req) {
        return coupleService.update(coupleId, CurrentUser.id(), req);
    }

    // ---- breakup (two-step) --------------------------------------------

    /** Step 1: initiator requests breakup, partner is notified. */
    @PostMapping("/{coupleId}/breakup")
    public BreakupPendingResponse initiateBreakup(@PathVariable String coupleId,
                                                  @RequestBody(required = false) InitiateBreakupRequest req) {
        return coupleService.initiateBreakup(coupleId, CurrentUser.id(),
                req != null ? req : new InitiateBreakupRequest(null));
    }

    /** Step 2: partner confirms → couple ends. */
    @PostMapping("/{coupleId}/breakup/confirm")
    public Map<String, Object> confirmBreakup(@PathVariable String coupleId) {
        return coupleService.confirmBreakup(coupleId, CurrentUser.id());
    }

    /** Initiator cancels the pending request. */
    @DeleteMapping("/{coupleId}/breakup")
    public Map<String, Object> cancelBreakup(@PathVariable String coupleId) {
        return coupleService.cancelBreakup(coupleId, CurrentUser.id());
    }

    /** Returns the active pending breakup request, if any. */
    @GetMapping("/{coupleId}/breakup/status")
    public ResponseEntity<BreakupPendingResponse> breakupStatus(@PathVariable String coupleId) {
        Optional<BreakupPendingResponse> status = coupleService.getBreakupStatus(coupleId, CurrentUser.id());
        return status.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    // ---- data export ----------------------------------------------------

    @GetMapping("/{coupleId}/export")
    public Map<String, Object> export(@PathVariable String coupleId, HttpServletRequest request) {
        String userId = CurrentUser.id();
        Map<String, Object> data = exportService.export(coupleId, userId);
        auditService.log(userId, coupleId, AuditAction.DATA_EXPORT, "COUPLE", coupleId,
                request.getRemoteAddr());
        return data;
    }
}
