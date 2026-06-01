package com.pairflow.mood;

import com.pairflow.mood.dto.AddReactionRequest;
import com.pairflow.mood.dto.CreateMoodRequest;
import com.pairflow.mood.dto.MoodResponse;
import com.pairflow.mood.dto.TodayMoodResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/moods")
public class MoodController {

    private final MoodService service;

    public MoodController(MoodService service) {
        this.service = service;
    }

    @GetMapping("/today")
    public TodayMoodResponse today() {
        return service.today();
    }

    @PostMapping
    public MoodResponse post(@Valid @RequestBody CreateMoodRequest req) {
        return service.post(req);
    }

    @GetMapping("/history")
    public List<MoodResponse> history() {
        return service.history();
    }

    @PostMapping("/{moodId}/reactions")
    public MoodResponse react(@PathVariable String moodId, @Valid @RequestBody AddReactionRequest req) {
        return service.react(moodId, req);
    }
}
