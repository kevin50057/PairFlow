package com.pairflow.dateplan;

import com.pairflow.common.enums.Priority;
import com.pairflow.common.error.ApiException;
import com.pairflow.couple.CoupleContext;
import com.pairflow.dateplan.dto.AddCandidateRequest;
import com.pairflow.dateplan.dto.CandidateResponse;
import com.pairflow.dateplan.dto.CreateDatePlanRequest;
import com.pairflow.dateplan.dto.DatePlanResponse;
import com.pairflow.dateplan.dto.FinalizeRequest;
import com.pairflow.dateplan.dto.VoteRequest;
import com.pairflow.dateplan.dto.VoteView;
import com.pairflow.event.EventType;
import com.pairflow.event.EventService;
import com.pairflow.event.dto.CreateEventRequest;
import com.pairflow.event.dto.EventResponse;
import com.pairflow.todo.AssigneeOption;
import com.pairflow.todo.TodoService;
import com.pairflow.todo.TodoType;
import com.pairflow.todo.dto.CreateTodoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DatePlanService {

    private final DatePlanRepository planRepository;
    private final DateCandidateRepository candidateRepository;
    private final DateVoteRepository voteRepository;
    private final EventService eventService;
    private final TodoService todoService;
    private final CoupleContext coupleContext;

    public DatePlanService(DatePlanRepository planRepository,
                           DateCandidateRepository candidateRepository,
                           DateVoteRepository voteRepository,
                           EventService eventService,
                           TodoService todoService,
                           CoupleContext coupleContext) {
        this.planRepository = planRepository;
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
        this.eventService = eventService;
        this.todoService = todoService;
        this.coupleContext = coupleContext;
    }

    @Transactional(readOnly = true)
    public List<DatePlanResponse> list() {
        String coupleId = coupleContext.requireCoupleId();
        return planRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DatePlanResponse get(String id) {
        return toResponse(load(id));
    }

    @Transactional
    public DatePlanResponse create(CreateDatePlanRequest req) {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();
        DatePlan plan = new DatePlan();
        plan.setCoupleId(coupleId);
        plan.setTitle(req.title().trim());
        plan.setDateType(req.dateType() != null ? req.dateType() : DateType.FOOD);
        plan.setBudgetLevel(req.budgetLevel());
        plan.setArea(req.area());
        plan.setDurationHours(req.durationHours());
        plan.setStatus(PlanStatus.PLANNING);
        plan.setCreatedBy(me);
        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public DatePlanResponse addCandidate(String planId, AddCandidateRequest req) {
        DatePlan plan = load(planId);
        DateCandidate c = new DateCandidate();
        c.setPlanId(plan.getId());
        c.setTitle(req.title().trim());
        c.setDescription(req.description());
        c.setLocation(req.location());
        c.setAddedBy(coupleContext.currentUserId());
        candidateRepository.save(c);
        return toResponse(plan);
    }

    @Transactional
    public DatePlanResponse vote(String planId, String candidateId, VoteRequest req) {
        DatePlan plan = load(planId);
        DateCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> ApiException.notFound("Candidate not found"));
        if (!candidate.getPlanId().equals(plan.getId())) {
            throw ApiException.notFound("Candidate not found");
        }
        String me = coupleContext.currentUserId();
        DateVote vote = voteRepository.findByCandidateIdAndUserId(candidateId, me).orElseGet(DateVote::new);
        vote.setCandidateId(candidateId);
        vote.setUserId(me);
        vote.setVote(req.vote());
        voteRepository.save(vote);
        return toResponse(plan);
    }

    /** Lock in a candidate → schedule a calendar Event and auto-create prep Todos (spec 7.11). */
    @Transactional
    public DatePlanResponse finalizePlan(String planId, FinalizeRequest req) {
        DatePlan plan = load(planId);
        DateCandidate chosen = candidateRepository.findById(req.candidateId())
                .orElseThrow(() -> ApiException.notFound("Candidate not found"));
        if (!chosen.getPlanId().equals(plan.getId())) {
            throw ApiException.notFound("Candidate not found");
        }

        CreateEventRequest eventReq = new CreateEventRequest(
                plan.getTitle() + "：" + chosen.getTitle(), chosen.getDescription(), EventType.DATE,
                req.startTime(), req.endTime(), chosen.getLocation(), null, null,
                null, null, null, null, null, null);
        EventResponse event = eventService.create(eventReq);

        plan.setChosenCandidateId(chosen.getId());
        plan.setScheduledEventId(event.id());
        plan.setStatus(PlanStatus.DECIDED);

        if (req.createTodos() == null || req.createTodos()) {
            for (String task : prepTasks(plan.getDateType())) {
                CreateTodoRequest todoReq = new CreateTodoRequest(
                        task, null, TodoType.DATE, Priority.MEDIUM, AssigneeOption.BOTH,
                        req.startTime(), null, null, null, null, event.id(), null, null, null, null, null);
                todoService.create(todoReq);
            }
        }
        return toResponse(plan);
    }

    @Transactional
    public void delete(String planId) {
        DatePlan plan = load(planId);
        candidateRepository.findByPlanIdOrderByCreatedAtAsc(plan.getId())
                .forEach(c -> voteRepository.deleteByCandidateId(c.getId()));
        candidateRepository.deleteByPlanId(plan.getId());
        planRepository.delete(plan);
    }

    // ---- helpers ---------------------------------------------------------

    private List<String> prepTasks(DateType type) {
        return switch (type) {
            case FOOD, CAFE, ANNIVERSARY, RELAX -> List.of("訂餐廳", "查交通", "確認時間");
            case MOVIE -> List.of("買電影票", "查場次", "查交通");
            case TRIP -> List.of("訂住宿", "查交通", "查天氣", "準備行李");
            case EXHIBITION -> List.of("買門票", "查交通", "查開放時間");
            case SPORT -> List.of("準備裝備", "查交通", "確認時間");
            default -> List.of("查交通", "確認時間");
        };
    }

    private DatePlan load(String id) {
        DatePlan plan = planRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Date plan not found"));
        if (!plan.getCoupleId().equals(coupleContext.requireCoupleId())) {
            throw ApiException.notFound("Date plan not found");
        }
        return plan;
    }

    private DatePlanResponse toResponse(DatePlan plan) {
        String me = coupleContext.currentUserId();
        List<CandidateResponse> candidates = candidateRepository.findByPlanIdOrderByCreatedAtAsc(plan.getId()).stream()
                .map(c -> {
                    List<DateVote> votes = voteRepository.findByCandidateId(c.getId());
                    List<VoteView> voteViews = votes.stream()
                            .map(v -> new VoteView(v.getUserId(), v.getVote().name())).toList();
                    String myVote = votes.stream().filter(v -> v.getUserId().equals(me))
                            .map(v -> v.getVote().name()).findFirst().orElse(null);
                    return new CandidateResponse(c.getId(), c.getPlanId(), c.getTitle(), c.getDescription(),
                            c.getLocation(), c.getAddedBy(), voteViews, myVote, c.getCreatedAt());
                })
                .toList();
        return new DatePlanResponse(
                plan.getId(), plan.getCoupleId(), plan.getTitle(), plan.getDateType().name(),
                plan.getBudgetLevel() != null ? plan.getBudgetLevel().name() : null,
                plan.getArea(), plan.getDurationHours(), plan.getStatus().name(),
                plan.getChosenCandidateId(), plan.getScheduledEventId(), candidates,
                plan.getCreatedBy(), plan.getCreatedAt());
    }
}
