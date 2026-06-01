package com.pairflow.event;

import com.pairflow.common.error.ApiException;
import com.pairflow.event.dto.CreateEventRequest;
import com.pairflow.event.dto.EventResponse;
import com.pairflow.event.dto.UpdateEventRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping
    public List<EventResponse> list(@RequestParam(required = false) String from,
                                    @RequestParam(required = false) String to) {
        return service.list(parseInstant(from), parseInstant(to));
    }

    @PostMapping
    public EventResponse create(@Valid @RequestBody CreateEventRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    public EventResponse update(@PathVariable String id, @Valid @RequestBody UpdateEventRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    private static Instant parseInstant(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return OffsetDateTime.parse(s).toInstant();
        } catch (Exception ignore) {
            // fall through
        }
        try {
            return Instant.parse(s);
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid datetime: " + s);
        }
    }
}
