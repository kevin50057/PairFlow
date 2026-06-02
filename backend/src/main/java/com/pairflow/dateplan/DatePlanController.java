package com.pairflow.dateplan;

import com.pairflow.dateplan.dto.AddCandidateRequest;
import com.pairflow.dateplan.dto.CreateDatePlanRequest;
import com.pairflow.dateplan.dto.DatePlanResponse;
import com.pairflow.dateplan.dto.FinalizeRequest;
import com.pairflow.dateplan.dto.VoteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/date-plans")
public class DatePlanController {

    private final DatePlanService service;

    public DatePlanController(DatePlanService service) {
        this.service = service;
    }

    @GetMapping
    public List<DatePlanResponse> list() {
        return service.list();
    }

    @PostMapping
    public DatePlanResponse create(@Valid @RequestBody CreateDatePlanRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public DatePlanResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/candidates")
    public DatePlanResponse addCandidate(@PathVariable String id, @Valid @RequestBody AddCandidateRequest req) {
        return service.addCandidate(id, req);
    }

    @PostMapping("/{id}/candidates/{candidateId}/vote")
    public DatePlanResponse vote(@PathVariable String id, @PathVariable String candidateId,
                                 @Valid @RequestBody VoteRequest req) {
        return service.vote(id, candidateId, req);
    }

    @PostMapping("/{id}/finalize")
    public DatePlanResponse finalizePlan(@PathVariable String id, @Valid @RequestBody FinalizeRequest req) {
        return service.finalizePlan(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
