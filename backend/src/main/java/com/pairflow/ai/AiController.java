package com.pairflow.ai;

import com.pairflow.ai.dto.AiDtos.AnniversaryMessageRequest;
import com.pairflow.ai.dto.AiDtos.DateSuggestionRequest;
import com.pairflow.ai.dto.AiDtos.ItemsResponse;
import com.pairflow.ai.dto.AiDtos.MemorySummaryRequest;
import com.pairflow.ai.dto.AiDtos.SoftenRequest;
import com.pairflow.ai.dto.AiDtos.SoftenResponse;
import com.pairflow.ai.dto.AiDtos.TextResponse;
import com.pairflow.ai.dto.AiDtos.TodoBreakdownRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService service;

    public AiController(AiService service) {
        this.service = service;
    }

    @PostMapping("/todo-breakdown")
    public ItemsResponse todoBreakdown(@Valid @RequestBody TodoBreakdownRequest req) {
        return new ItemsResponse(service.breakdownTodo(req.input()));
    }

    @PostMapping("/date-suggestions")
    public ItemsResponse dateSuggestions(@RequestBody DateSuggestionRequest req) {
        return new ItemsResponse(service.dateSuggestions(req.dateType(), req.budget(), req.area(), req.mood()));
    }

    @PostMapping("/anniversary-message")
    public TextResponse anniversaryMessage(@Valid @RequestBody AnniversaryMessageRequest req) {
        return new TextResponse(service.anniversaryMessage(req.occasion(), req.tone()));
    }

    @PostMapping("/soften")
    public SoftenResponse soften(@Valid @RequestBody SoftenRequest req) {
        return service.soften(req.text());
    }

    @PostMapping("/memory-summary")
    public TextResponse memorySummary(@Valid @RequestBody MemorySummaryRequest req) {
        return new TextResponse(service.memorySummary(req.context()));
    }

    @GetMapping("/nudges")
    public ItemsResponse nudges() {
        return new ItemsResponse(service.nudges());
    }
}
