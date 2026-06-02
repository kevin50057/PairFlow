package com.pairflow.ai;

import com.pairflow.ai.dto.AiDtos.SoftenResponse;
import com.pairflow.anniversary.AnniversaryService;
import com.pairflow.anniversary.dto.AnniversaryResponse;
import com.pairflow.album.Photo;
import com.pairflow.album.PhotoRepository;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleContext;
import com.pairflow.mood.MoodEntry;
import com.pairflow.mood.MoodEntryRepository;
import com.pairflow.mood.MoodType;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
public class AiService {

    private static final EnumSet<MoodType> LOW_MOODS =
            EnumSet.of(MoodType.TIRED, MoodType.STRESSED, MoodType.SAD, MoodType.ANGRY);

    private final AiProvider provider;
    private final SafetyGuard safetyGuard;
    private final AnniversaryService anniversaryService;
    private final MoodEntryRepository moodRepository;
    private final PhotoRepository photoRepository;
    private final CoupleContext coupleContext;

    public AiService(AiProvider provider, SafetyGuard safetyGuard, AnniversaryService anniversaryService,
                     MoodEntryRepository moodRepository, PhotoRepository photoRepository, CoupleContext coupleContext) {
        this.provider = provider;
        this.safetyGuard = safetyGuard;
        this.anniversaryService = anniversaryService;
        this.moodRepository = moodRepository;
        this.photoRepository = photoRepository;
        this.coupleContext = coupleContext;
    }

    public List<String> breakdownTodo(String input) {
        return provider.breakdownTodo(input);
    }

    public List<String> dateSuggestions(String dateType, String budget, String area, String mood) {
        return provider.dateSuggestions(dateType, budget, area, mood);
    }

    public String anniversaryMessage(String occasion, String tone) {
        return provider.anniversaryMessage(occasion, tone);
    }

    public String memorySummary(String context) {
        return provider.memorySummary(context);
    }

    /** Softens a message — but if it signals real danger, stop and surface help instead (spec 7.10). */
    public SoftenResponse soften(String text) {
        if (safetyGuard.isHighRisk(text)) {
            return new SoftenResponse(text, null, true, safetyGuard.helpNotice());
        }
        return new SoftenResponse(text, provider.softenText(text), false, null);
    }

    /** Gentle, opt-in relationship reminders derived from real signals (spec 7.14 #6). */
    @Transactional(readOnly = true)
    public List<String> nudges() {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);
        List<String> nudges = new ArrayList<>();

        AnniversaryResponse next = anniversaryService.nextUpcoming(couple.getId());
        if (next != null && next.daysLeft() >= 0 && next.daysLeft() <= 7) {
            nudges.add("距離「" + next.title() + "」還有 " + next.daysLeft() + " 天，要不要開始準備？");
        }

        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        long lowMoodCount = moodRepository
                .findByCoupleIdOrderByCreatedAtDesc(couple.getId(), PageRequest.of(0, 50)).stream()
                .filter(m -> m.getUserId().equals(partnerId))
                .filter(m -> m.getCreatedAt().isAfter(weekAgo))
                .filter(m -> LOW_MOODS.contains(m.getMood()))
                .count();
        if (lowMoodCount >= 2) {
            nudges.add("對方這週的心情比較低落，也許可以主動關心一下。");
        }

        List<Photo> photos = photoRepository.findByCoupleIdOrderByCreatedAtDesc(couple.getId());
        if (photos.isEmpty()) {
            nudges.add("還沒有任何共同回憶，要不要一起拍下第一張照片？");
        } else {
            long daysSince = ChronoUnit.DAYS.between(
                    photos.get(0).getCreatedAt().atZone(AppTime.ZONE).toLocalDate(), AppTime.today());
            if (daysSince >= 20) {
                nudges.add("你們已經 " + daysSince + " 天沒有新增共同回憶了，要不要安排一次小約會？");
            }
        }

        if (nudges.isEmpty()) {
            nudges.add("你們最近互動得很好，繼續保持 ❤️");
        }
        return nudges;
    }
}
