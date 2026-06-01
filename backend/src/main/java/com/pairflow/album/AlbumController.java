package com.pairflow.album;

import com.pairflow.album.dto.AlbumResponse;
import com.pairflow.album.dto.CreateAlbumRequest;
import com.pairflow.album.dto.UpdateAlbumRequest;
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
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService service;

    public AlbumController(AlbumService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlbumResponse> list() {
        return service.list();
    }

    @PostMapping
    public AlbumResponse create(@Valid @RequestBody CreateAlbumRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}")
    public AlbumResponse update(@PathVariable String id, @Valid @RequestBody UpdateAlbumRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
