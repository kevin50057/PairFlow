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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
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

    public HomeService(CoupleContext coupleContext,
                       MoodService moodService,
                       TodoService todoService,
                       AnniversaryService anniversaryService,
                       EventService eventService,
                       MemoryProvider memoryProvider) {
        this.coupleContext = coupleContext;
        this.moodService = moodService;
        this.todoService = todoService;
        this.anniversaryService = anniversaryService;
        this.eventService = eventService;
        this.memoryProvider = memoryProvider;
    }

    @Transactional(readOnly = true)
    public HomeResponse home() {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);

        LocalDate start = couple.getRelationshipStartDate();
        Long daysTogether = start != null ? ChronoUnit.DAYS.between(start, AppTime.today()) : null;
        HomeCouple coupleSummary = new HomeCouple(daysTogether, start);

        MoodResponse partnerMood = moodService.partnerToday(couple.getId(), partnerId);
        List<TodoResponse> todayTodos = todoService.todayTodos();

        AnniversaryResponse next = anniversaryService.nextUpcoming(couple.getId());
        HomeAnniversary nextAnniversary = next == null ? null
                : new HomeAnniversary(next.title(), next.daysLeft(), next.nextOccurrence());

        LocalDate today = AppTime.today();
        Instant dayStart = today.atStartOfDay(AppTime.ZONE).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(AppTime.ZONE).toInstant();
        List<EventResponse> todayEvents = eventService.between(couple.getId(), dayStart, dayEnd);

        return new HomeResponse(
                coupleSummary, partnerMood, todayTodos, nextAnniversary, todayEvents,
                memoryProvider.onThisDay(couple.getId(), me));
    }
}
