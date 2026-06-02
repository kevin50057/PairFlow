package com.pairflow.repair;

import com.pairflow.ai.AiService;
import com.pairflow.ai.SafetyGuard;
import com.pairflow.ai.dto.AiDtos.SoftenResponse;
import com.pairflow.common.enums.Priority;
import com.pairflow.common.error.ApiException;
import com.pairflow.couple.CoupleContext;
import com.pairflow.repair.dto.CreateRepairRequest;
import com.pairflow.repair.dto.FollowUpRequest;
import com.pairflow.repair.dto.RepairResponse;
import com.pairflow.repair.dto.RespondRequest;
import com.pairflow.todo.AssigneeOption;
import com.pairflow.todo.TodoService;
import com.pairflow.todo.TodoType;
import com.pairflow.todo.dto.CreateTodoRequest;
import com.pairflow.todo.dto.TodoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepairService {

    private final RepairSessionRepository repository;
    private final AiService aiService;
    private final SafetyGuard safetyGuard;
    private final TodoService todoService;
    private final CoupleContext coupleContext;

    public RepairService(RepairSessionRepository repository, AiService aiService, SafetyGuard safetyGuard,
                         TodoService todoService, CoupleContext coupleContext) {
        this.repository = repository;
        this.aiService = aiService;
        this.safetyGuard = safetyGuard;
        this.todoService = todoService;
        this.coupleContext = coupleContext;
    }

    @Transactional
    public RepairResponse create(CreateRepairRequest req) {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();

        RepairSession s = new RepairSession();
        s.setCoupleId(coupleId);
        s.setInitiatorId(me);
        s.setState(req.state());
        s.setFeelings(req.feelings());
        s.setKeyPoints(req.keyPoints());

        SoftenResponse soft = aiService.soften(req.feelings());
        s.setFlagged(soft.flagged());
        if (!soft.flagged()) {
            s.setSoftenedMessage(soft.softened());
        }
        s.setStatus(RepairStatus.DRAFT);
        repository.save(s);
        return toResponse(s, me);
    }

    @Transactional(readOnly = true)
    public List<RepairResponse> list() {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();
        return repository.findByCoupleIdOrderByCreatedAtDesc(coupleId).stream()
                .filter(s -> s.getInitiatorId().equals(me) || s.getStatus() != RepairStatus.DRAFT)
                .map(s -> toResponse(s, me))
                .toList();
    }

    @Transactional(readOnly = true)
    public RepairResponse get(String id) {
        return toResponse(loadVisible(id), coupleContext.currentUserId());
    }

    @Transactional
    public RepairResponse send(String id) {
        String me = coupleContext.currentUserId();
        RepairSession s = loadVisible(id);
        if (!s.getInitiatorId().equals(me)) {
            throw ApiException.forbidden("Only the initiator can send this");
        }
        if (s.isFlagged()) {
            throw ApiException.badRequest("This message was flagged for safety — please seek human help first");
        }
        if (s.getSoftenedMessage() == null) {
            throw ApiException.badRequest("Nothing to send");
        }
        s.setStatus(RepairStatus.SENT);
        return toResponse(s, me);
    }

    @Transactional
    public RepairResponse respond(String id, RespondRequest req) {
        String me = coupleContext.currentUserId();
        RepairSession s = loadVisible(id);
        if (s.getInitiatorId().equals(me)) {
            throw ApiException.badRequest("Only your partner can respond to this");
        }
        if (s.getStatus() == RepairStatus.DRAFT) {
            throw ApiException.notFound("Repair session not found");
        }
        s.setResponderId(me);
        s.setResponseType(req.responseType());
        s.setResponseNote(req.note());
        s.setRespondedAt(Instant.now());
        s.setStatus(RepairStatus.RESPONDED);
        return toResponse(s, me);
    }

    @Transactional
    public List<TodoResponse> followUps(String id, FollowUpRequest req) {
        RepairSession s = loadVisible(id);
        List<TodoResponse> created = new ArrayList<>();
        for (String task : req.tasks()) {
            if (task == null || task.isBlank()) {
                continue;
            }
            CreateTodoRequest todoReq = new CreateTodoRequest(
                    task.trim(), "來自修復對話的後續", TodoType.GENERAL, Priority.MEDIUM, AssigneeOption.BOTH,
                    null, null, null, null, null, null, null, null, null, null, null);
            created.add(todoService.create(todoReq));
        }
        if (s.getStatus() == RepairStatus.RESPONDED) {
            s.setStatus(RepairStatus.CLOSED);
        }
        return created;
    }

    // ---- helpers ---------------------------------------------------------

    private RepairSession loadVisible(String id) {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();
        RepairSession s = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Repair session not found"));
        if (!s.getCoupleId().equals(coupleId)) {
            throw ApiException.notFound("Repair session not found");
        }
        // The partner can't see drafts — only the initiator works on a draft.
        if (!s.getInitiatorId().equals(me) && s.getStatus() == RepairStatus.DRAFT) {
            throw ApiException.notFound("Repair session not found");
        }
        return s;
    }

    private RepairResponse toResponse(RepairSession s, String viewerId) {
        boolean isInitiator = s.getInitiatorId().equals(viewerId);
        String notice = (s.isFlagged() && isInitiator) ? safetyGuard.helpNotice() : null;
        return new RepairResponse(
                s.getId(), s.getCoupleId(), s.getInitiatorId(), s.getState().name(),
                isInitiator ? s.getFeelings() : null,   // raw feelings stay with the initiator
                s.getKeyPoints(), s.getSoftenedMessage(), s.getStatus().name(), s.isFlagged(), notice,
                s.getResponderId(),
                s.getResponseType() != null ? s.getResponseType().name() : null,
                s.getResponseNote(), s.getRespondedAt(), s.getCreatedAt());
    }
}
