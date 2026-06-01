package com.pairflow.couple;

import com.pairflow.common.error.ApiException;
import com.pairflow.common.error.ErrorCode;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.dto.BreakupRequest;
import com.pairflow.couple.dto.CoupleResponse;
import com.pairflow.couple.dto.CreateInviteResponse;
import com.pairflow.couple.dto.UpdateCoupleRequest;
import com.pairflow.user.User;
import com.pairflow.user.UserRepository;
import com.pairflow.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CoupleService {

    // No ambiguous characters (no I, L, O, 0, 1).
    private static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final long INVITE_TTL_DAYS = 7;

    private final CoupleRepository coupleRepository;
    private final CoupleInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public CoupleService(CoupleRepository coupleRepository,
                         CoupleInviteRepository inviteRepository,
                         UserRepository userRepository) {
        this.coupleRepository = coupleRepository;
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
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

        return toResponse(couple, userId);
    }

    // ---- read / update ---------------------------------------------------

    @Transactional(readOnly = true)
    public CoupleResponse getMyCouple(String userId) {
        return toResponse(requireActiveCouple(userId), userId);
    }

    /** Used by every couple-scoped module to resolve & authorize the caller's couple. */
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

    @Transactional
    public Map<String, Object> breakup(String coupleId, String userId, BreakupRequest req) {
        if (req.confirm() == null || !req.confirm()) {
            throw ApiException.badRequest("Breakup requires explicit confirmation");
        }
        Couple couple = loadMember(coupleId, userId);
        if (couple.getStatus() == CoupleStatus.ENDED) {
            throw ApiException.conflict("This couple space has already ended");
        }
        couple.setStatus(CoupleStatus.ENDED);
        couple.setEndedAt(Instant.now());
        couple.setDataHandling(req.dataHandling() != null ? req.dataHandling() : DataHandling.ARCHIVE);

        // Shared-data export/deletion per the chosen option is handled by a follow-up
        // routine (see PrivacyService) so each member can also keep a personal copy.
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("coupleId", coupleId);
        result.put("status", couple.getStatus().name());
        result.put("dataHandling", couple.getDataHandling().name());
        return result;
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
        Long daysTogether = start != null ? ChronoUnit.DAYS.between(start, AppTime.today()) : null;
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
