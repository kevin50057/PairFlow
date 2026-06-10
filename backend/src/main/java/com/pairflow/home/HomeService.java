package com.pairflow.home;

import com.pairflow.anniversary.AnniversaryService;
import com.pairflow.anniversary.dto.AnniversaryResponse;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleContext;
import com.pairflow.event.EventService;
import com.pairflow.event.dto.EventResponse;
import com.pairflow.home.dto.HomeAnniversary;
import com.pairflow.home.dto.HomeCouple;
import com.pairflow.home.dto.HomeResponse;
import com.pairflow.memory.MemoryProvider;
import com.pairflow.mood.MoodService;
import com.pairflow.mood.dto.MoodResponse;
import com.pairflow.todo.TodoService;
import com.pairflow.todo.dto.TodoResponse;
import com.pairflow.user.User;
import com.pairflow.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HomeService {

    private final CoupleContext coupleContext;
    private final MoodService moodService;
    private final TodoService todoService;
    private final AnniversaryService anniversaryService;
    private final EventService eventService;
    private final MemoryProvider memoryProvider;
    private final UserRepository userRepository;

    public HomeService(CoupleContext coupleContext,
                       MoodService moodService,
                       TodoService todoService,
                       AnniversaryService anniversaryService,
                       EventService eventService,
                       MemoryProvider memoryProvider,
                       UserRepository userRepository) {
        this.coupleContext = coupleContext;
        this.moodService = moodService;
        this.todoService = todoService;
        this.anniversaryService = anniversaryService;
        this.eventService = eventService;
        this.memoryProvider = memoryProvider;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public HomeResponse home() {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);

        LocalDate start = couple.getRelationshipStartDate();
        Long daysTogether = start != null ? ChronoUnit.DAYS.between(start, AppTime.today()) + 1 : null;
        HomeCouple coupleSummary = new HomeCouple(daysTogether, start);

        MoodResponse partnerMood = moodService.partnerToday(couple.getId(), partnerId);
        List<TodoResponse> todayTodos = todoService.todayTodos();

        HomeAnniversary nextAnniversary = soonestImportantDay(couple.getId(), me, partnerId);

        LocalDate today = AppTime.today();
        Instant dayStart = today.atStartOfDay(AppTime.ZONE).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(AppTime.ZONE).toInstant();
        List<EventResponse> todayEvents = eventService.between(couple.getId(), dayStart, dayEnd);

        return new HomeResponse(
                coupleSummary, partnerMood, todayTodos, nextAnniversary, todayEvents,
                memoryProvider.onThisDay(couple.getId(), me));
    }

    /** Soonest of: manual anniversaries, my birthday, partner's birthday. */
    private HomeAnniversary soonestImportantDay(String coupleId, String meId, String partnerId) {
        LocalDate today = AppTime.today();
        AnniversaryResponse next = anniversaryService.nextUpcoming(coupleId);
        HomeAnniversary best = next == null ? null
                : new HomeAnniversary(next.title(), next.daysLeft(), next.nextOccurrence());
        best = closer(best, birthdayDay(partnerId, today));
        best = closer(best, birthdayDay(meId, today));
        return best;
    }

    private HomeAnniversary birthdayDay(String userId, LocalDate today) {
        User u = userId == null ? null : userRepository.findById(userId).orElse(null);
        if (u == null || u.getBirthday() == null) return null;
        MonthDay md = MonthDay.from(u.getBirthday());
        LocalDate next = md.atYear(today.getYear());
        if (next.isBefore(today)) next = md.atYear(today.getYear() + 1);
        long daysLeft = ChronoUnit.DAYS.between(today, next);
        return new HomeAnniversary(u.getDisplayName() + " 生日", daysLeft, next);
    }

    private HomeAnniversary closer(HomeAnniversary a, HomeAnniversary b) {
        if (a == null) return b;
        if (b == null) return a;
        return b.daysLeft() < a.daysLeft() ? b : a;
    }
}
