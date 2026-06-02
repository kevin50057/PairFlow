package com.pairflow.repair;

import com.pairflow.repair.dto.CreateRepairRequest;
import com.pairflow.repair.dto.FollowUpRequest;
import com.pairflow.repair.dto.RepairResponse;
import com.pairflow.repair.dto.RespondRequest;
import com.pairflow.todo.dto.TodoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    private final RepairService service;

    public RepairController(RepairService service) {
        this.service = service;
    }

    @GetMapping
    public List<RepairResponse> list() {
        return service.list();
    }

    @PostMapping
    public RepairResponse create(@Valid @RequestBody CreateRepairRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public RepairResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/send")
    public RepairResponse send(@PathVariable String id) {
        return service.send(id);
    }

    @PostMapping("/{id}/respond")
    public RepairResponse respond(@PathVariable String id, @Valid @RequestBody RespondRequest req) {
        return service.respond(id, req);
    }

    @PostMapping("/{id}/follow-ups")
    public List<TodoResponse> followUps(@PathVariable String id, @Valid @RequestBody FollowUpRequest req) {
        return service.followUps(id, req);
    }
}
