package com.pairflow.question;

import com.pairflow.question.dto.AnswerRequest;
import com.pairflow.question.dto.DailyQuestionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @GetMapping("/today")
    public DailyQuestionResponse today() {
        return service.today();
    }

    @PostMapping("/today/answer")
    public DailyQuestionResponse answer(@Valid @RequestBody AnswerRequest req) {
        return service.answer(req);
    }

    @GetMapping("/history")
    public List<DailyQuestionResponse> history() {
        return service.history();
    }

    @PostMapping("/{id}/favorite")
    public DailyQuestionResponse toggleFavorite(@PathVariable String id) {
        return service.toggleFavorite(id);
    }
}
