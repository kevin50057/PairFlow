package com.pairflow.album;

import com.pairflow.album.dto.PhotoResponse;
import com.pairflow.album.dto.UpdatePhotoRequest;
import com.pairflow.common.error.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService service;

    public PhotoController(PhotoService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PhotoResponse upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String albumId,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) String takenAt,
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) List<String> tags) {
        return service.upload(file, albumId, caption, parseInstant(takenAt), locationName, tags);
    }

    @GetMapping
    public List<PhotoResponse> list(@RequestParam(required = false) String albumId,
                                    @RequestParam(required = false) Boolean favorite) {
        return service.list(albumId, favorite);
    }

    @PatchMapping("/{id}")
    public PhotoResponse update(@PathVariable String id, @Valid @RequestBody UpdatePhotoRequest req) {
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
