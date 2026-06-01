package com.pairflow.note;

import com.pairflow.note.dto.CreateNoteRequest;
import com.pairflow.note.dto.NoteResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<NoteResponse> list() {
        return service.list();
    }

    @PostMapping
    public NoteResponse create(@Valid @RequestBody CreateNoteRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/read")
    public NoteResponse markRead(@PathVariable String id) {
        return service.markRead(id);
    }

    @PostMapping("/{id}/favorite")
    public NoteResponse toggleFavorite(@PathVariable String id) {
        return service.toggleFavorite(id);
    }
}
