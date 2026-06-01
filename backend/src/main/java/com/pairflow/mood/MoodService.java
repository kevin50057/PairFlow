package com.pairflow.mood;

import com.pairflow.common.error.ApiException;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleContext;
import com.pairflow.mood.dto.AddReactionRequest;
import com.pairflow.mood.dto.CreateMoodRequest;
import com.pairflow.mood.dto.MoodResponse;
import com.pairflow.mood.dto.ReactionResponse;
import com.pairflow.mood.dto.TodayMoodResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MoodService {

    private final MoodEntryRepository moodRepository;
    private final MoodReactionRepository reactionRepository;
    private final CoupleContext coupleContext;

    public MoodService(MoodEntryRepository moodRepository,
                       MoodReactionRepository reactionRepository,
                       CoupleContext coupleContext) {
        this.moodRepository = moodRepository;
        this.reactionRepository = reactionRepository;
        this.coupleContext = coupleContext;
    }

    @Transactional
    public MoodResponse post(CreateMoodRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        MoodEntry entry = new MoodEntry();
        entry.setCoupleId(couple.getId());
        entry.setUserId(me);
        entry.setMood(req.mood());
        entry.setEmoji(req.emoji());
        entry.setNote(req.note());
        entry.setNeedResponse(Boolean.TRUE.equals(req.needResponse()));
        return toResponse(moodRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public TodayMoodResponse today() {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);
        return new TodayMoodResponse(
                latestToday(couple.getId(), me).map(this::toResponse).orElse(null),
                latestToday(couple.getId(), partnerId).map(this::toResponse).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<MoodResponse> history() {
        String coupleId = coupleContext.requireCoupleId();
        return moodRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId, PageRequest.of(0, 60))
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public MoodResponse react(String moodId, AddReactionRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        MoodEntry entry = moodRepository.findById(moodId)
                .orElseThrow(() -> ApiException.notFound("Mood not found"));
        if (!entry.getCoupleId().equals(couple.getId())) {
            throw ApiException.notFound("Mood not found");
        }
        if (entry.getUserId().equals(me)) {
            throw ApiException.badRequest("You can only respond to your partner's mood");
        }
        MoodReaction reaction = new MoodReaction();
        reaction.setMoodEntryId(entry.getId());
        reaction.setUserId(me);
        reaction.setReaction(req.reaction());
        reactionRepository.save(reaction);
        return toResponse(entry);
    }

    /** Partner's latest mood today — for the home dashboard (spec 22). */
    @Transactional(readOnly = true)
    public MoodResponse partnerToday(String coupleId, String partnerId) {
        return latestToday(coupleId, partnerId).map(this::toResponse).orElse(null);
    }

    // ---- helpers ---------------------------------------------------------

    private Optional<MoodEntry> latestToday(String coupleId, String userId) {
        LocalDate today = AppTime.today();
        Instant start = today.atStartOfDay(AppTime.ZONE).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(AppTime.ZONE).toInstant();
        return moodRepository.findFirstByCoupleIdAndUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                coupleId, userId, start, end);
    }

    private MoodResponse toResponse(MoodEntry entry) {
        List<ReactionResponse> reactions = reactionRepository.findByMoodEntryIdOrderByCreatedAtAsc(entry.getId())
                .stream().map(ReactionResponse::from).toList();
        return new MoodResponse(
                entry.getId(), entry.getUserId(), entry.getMood().name(),
                entry.getEmoji(), entry.getNote(), entry.isNeedResponse(),
                reactions, entry.getCreatedAt());
    }
}
