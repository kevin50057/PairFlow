package com.pairflow.couple;

import com.pairflow.audit.AuditAction;
import com.pairflow.audit.AuditService;
import com.pairflow.common.error.ApiException;
import com.pairflow.common.error.ErrorCode;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.dto.BreakupPendingResponse;
import com.pairflow.couple.dto.CoupleResponse;
import com.pairflow.couple.dto.CreateInviteResponse;
import com.pairflow.couple.dto.InitiateBreakupRequest;
import com.pairflow.couple.dto.UpdateCoupleRequest;
import com.pairflow.notification.NotificationService;
import com.pairflow.notification.NotificationType;
import com.pairflow.user.User;
import com.pairflow.user.UserRepository;
import com.pairflow.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
public class CoupleService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final long INVITE_TTL_DAYS = 7;
    private static final long BREAKUP_TTL_DAYS = 7;

    private final CoupleRepository coupleRepository;
    private final CoupleInviteRepository inviteRepository;
    private final PendingBreakupRepository pendingBreakupRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final SecureRandom random = new SecureRandom();

    public CoupleService(CoupleRepository coupleRepository,
                         CoupleInviteRepository inviteRepository,
                         PendingBreakupRepository pendingBreakupRepository,
                         UserRepository userRepository,
                         AuditService auditService,
                         NotificationService notificationService) {
        this.coupleRepository = coupleRepository;
        this.inviteRepository = inviteRepository;
        this.pendingBreakupRepository = pendingBreakupRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    // ---- pairing ---------------------------------------------------------

    @Transactional
    public CreateInviteResponse createInvite(String userId) {
        requireNotCoupled(userId, "You are already in a couple");
        Optional<CoupleInvite> existing =
                inviteRepository.findFirstByInviterUserIdAndStatusOrderByCreatedAtDesc(userId, InviteStatus.PENDING);
        Instant now = Instant.now();
        if (existing.isPresent() && existing.get().getExpiresAt().isAfter(now)) {
            CoupleInvite inv = existing.get();
            return new CreateInviteResponse(inv.getCode(), inv.getExpiresAt());
        }
        CoupleInvite invite = new CoupleInvite();
        invite.setCode(generateUniqueCode());
        invite.setInviterUserId(userId);
        invite.setExpiresAt(now.plus(INVITE_TTL_DAYS, ChronoUnit.DAYS));
        invite.setStatus(InviteStatus.PENDING);
        invite = inviteRepository.save(invite);
        return new CreateInviteResponse(invite.getCode(), invite.getExpiresAt());
    }

    @Transactional
    public CoupleResponse join(String userId, String rawCode) {
        CoupleInvite invite = inviteRepository.findByCode(normalizeCode(rawCode))
                .orElseThrow(() -> ApiException.notFound("Invite code not found"));
        if (invite.getStatus() != InviteStatus.PENDING || invite.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.conflict("Invite code is no longer valid");
        }
        String inviterId = invite.getInviterUserId();
        if (inviterId.equals(userId)) {
            throw ApiException.badRequest("You cannot pair with yourself");
        }
        requireNotCoupled(userId, "You are already in a couple");
        requireNotCoupled(inviterId, "The inviter is already in a couple");

        Couple couple = new Couple();
        couple.setUserAId(inviterId);
        couple.setUserBId(userId);
        couple.setStatus(CoupleStatus.ACTIVE);
        couple = coupleRepository.save(couple);

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setAcceptedByUserId(userId);
        invite.setCoupleId(couple.getId());

        auditService.log(userId, couple.getId(), AuditAction.COUPLE_JOIN, "COUPLE", couple.getId(), null);
        return toResponse(couple, userId);
    }

    // ---- read / update ---------------------------------------------------

    @Transactional(readOnly = true)
    public CoupleResponse getMyCouple(String userId) {
        return toResponse(requireActiveCouple(userId), userId);
    }

    @Transactional(readOnly = true)
    public Couple requireActiveCouple(String userId) {
        return coupleRepository.findByStatusAndMember(CoupleStatus.ACTIVE, userId)
                .orElseThrow(() -> ApiException.notFound("No active couple space — pair with your partner first"));
    }

    @Transactional
    public CoupleResponse update(String coupleId, String userId, UpdateCoupleRequest req) {
        Couple couple = loadMember(coupleId, userId);
        if (req.relationshipStartDate() != null) {
            couple.setRelationshipStartDate(req.relationshipStartDate());
        }
        return toResponse(couple, userId);
    }

    // ---- breakup (two-step, spec 7.1 / 9.3) -----------------------------

    @Transactional
    public BreakupPendingResponse initiateBreakup(String coupleId, String userId, InitiateBreakupRequest req) {
        Couple couple = loadMember(coupleId, userId);
        if (couple.getStatus() == CoupleStatus.ENDED) {
            throw ApiException.conflict("This couple space has already ended");
        }
        // Check no existing pending request
        pendingBreakupRepository
                .findFirstByCoupleIdAndCancelledFalseAndConfirmedFalseOrderByCreatedAtDesc(coupleId)
                .ifPresent(pb -> {
                    if (pb.getExpiresAt().isAfter(Instant.now())) {
                        throw ApiException.conflict("A breakup request is already pending");
                    }
                });

        PendingBreakup pb = new PendingBreakup();
        pb.setCoupleId(coupleId);
        pb.setInitiatorId(userId);
        pb.setDataHandling(req.dataHandling() != null ? req.dataHandling() : DataHandling.ARCHIVE);
        pb.setExpiresAt(Instant.now().plus(BREAKUP_TTL_DAYS, ChronoUnit.DAYS));
        pendingBreakupRepository.save(pb);

        // Notify the other partner
        String partnerId = couple.partnerOf(userId);
        notificationService.notify(coupleId, partnerId, NotificationType.BREAKUP_REQUESTED,
                "解除綁定請求", "你的伴侶發出了解除綁定請求，請在 7 天內確認或等待自動取消。",
                "COUPLE", coupleId);

        auditService.log(userId, coupleId, AuditAction.COUPLE_BREAKUP_INITIATE, "COUPLE", coupleId, null);
        return BreakupPendingResponse.from(pb);
    }

    @Transactional
    public Map<String, Object> confirmBreakup(String coupleId, String userId) {
        Couple couple = loadMember(coupleId, userId);
        if (couple.getStatus() == CoupleStatus.ENDED) {
            throw ApiException.conflict("This couple space has already ended");
        }
        PendingBreakup pb = pendingBreakupRepository
                .findFirstByCoupleIdAndCancelledFalseAndConfirmedFalseOrderByCreatedAtDesc(coupleId)
                .orElseThrow(() -> ApiException.notFound("No pending breakup request"));
        if (pb.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.conflict("Breakup request has expired");
        }
        if (pb.getInitiatorId().equals(userId)) {
            throw ApiException.badRequest("The initiator cannot confirm their own breakup request; the partner must confirm");
        }

        pb.setConfirmed(true);
        pb.setConfirmedAt(Instant.now());
        pb.setConfirmedById(userId);

        couple.setStatus(CoupleStatus.ENDED);
        couple.setEndedAt(Instant.now());
        couple.setDataHandling(pb.getDataHandling());

        notificationService.notify(coupleId, pb.getInitiatorId(), NotificationType.BREAKUP_CONFIRMED,
                "解除綁定完成", "你的伴侶確認了解除綁定。感謝你們曾經一起走過的日子。",
                "COUPLE", coupleId);

        auditService.log(userId, coupleId, AuditAction.COUPLE_BREAKUP_CONFIRM, "COUPLE", coupleId, null);
        return Map.of("ok", true, "coupleId", coupleId, "status", "ENDED",
                "dataHandling", pb.getDataHandling().name());
    }

    @Transactional
    public Map<String, Object> cancelBreakup(String coupleId, String userId) {
        loadMember(coupleId, userId);
        PendingBreakup pb = pendingBreakupRepository
                .findFirstByCoupleIdAndCancelledFalseAndConfirmedFalseOrderByCreatedAtDesc(coupleId)
                .orElseThrow(() -> ApiException.notFound("No pending breakup request"));
        if (!pb.getInitiatorId().equals(userId)) {
            throw ApiException.forbidden("Only the initiator can cancel the breakup request");
        }
        pb.setCancelled(true);
        pb.setCancelledAt(Instant.now());

        Couple couple = coupleRepository.findById(coupleId).orElseThrow();
        String partnerId = couple.partnerOf(userId);
        notificationService.notify(coupleId, partnerId, NotificationType.BREAKUP_CANCELLED,
                "解除綁定取消", "解除綁定請求已被取消。",
                "COUPLE", coupleId);

        auditService.log(userId, coupleId, AuditAction.COUPLE_BREAKUP_CANCEL, "COUPLE", coupleId, null);
        return Map.of("ok", true, "cancelled", true);
    }

    @Transactional(readOnly = true)
    public Optional<BreakupPendingResponse> getBreakupStatus(String coupleId, String userId) {
        loadMember(coupleId, userId);
        return pendingBreakupRepository
                .findFirstByCoupleIdAndCancelledFalseAndConfirmedFalseOrderByCreatedAtDesc(coupleId)
                .filter(pb -> pb.getExpiresAt().isAfter(Instant.now()))
                .map(BreakupPendingResponse::from);
    }

    // ---- helpers ---------------------------------------------------------

    private Couple loadMember(String coupleId, String userId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> ApiException.notFound("Couple not found"));
        if (!couple.hasMember(userId)) {
            throw ApiException.forbidden("Not a member of this couple");
        }
        return couple;
    }

    private void requireNotCoupled(String userId, String message) {
        coupleRepository.findByStatusAndMember(CoupleStatus.ACTIVE, userId).ifPresent(c -> {
            throw ApiException.conflict(message);
        });
    }

    @Transactional(readOnly = true)
    public CoupleResponse toResponse(Couple couple, String viewerUserId) {
        String partnerId = couple.partnerOf(viewerUserId);
        User partner = userRepository.findById(partnerId).orElse(null);
        LocalDate start = couple.getRelationshipStartDate();
        Long daysTogether = start != null ? ChronoUnit.DAYS.between(start, AppTime.today()) + 1 : null;
        return new CoupleResponse(
                couple.getId(),
                partner != null ? UserResponse.from(partner) : null,
                start,
                daysTogether,
                couple.getStatus().name(),
                couple.getCreatedAt());
    }

    private String normalizeCode(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase();
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            String code = sb.toString();
            if (!inviteRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new ApiException(ErrorCode.INTERNAL_ERROR, "Could not generate a unique invite code");
    }
}
