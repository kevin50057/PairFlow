package com.pairflow.anniversary;

import com.pairflow.anniversary.dto.AnniversaryResponse;
import com.pairflow.anniversary.dto.CreateAnniversaryRequest;
import com.pairflow.anniversary.dto.UpdateAnniversaryRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/anniversaries")
public class AnniversaryController {

    private final AnniversaryService service;

    public AnniversaryController(AnniversaryService service) {
        this.service = service;
    }

    @GetMapping
    public List<AnniversaryResponse> list() {
        return service.list();
    }

    @PostMapping
    public AnniversaryResponse create(@Valid @RequestBody CreateAnniversaryRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}")
    public AnniversaryResponse update(@PathVariable String id, @Valid @RequestBody UpdateAnniversaryRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
