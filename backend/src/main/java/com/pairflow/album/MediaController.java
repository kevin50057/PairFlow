package com.pairflow.album;

import com.pairflow.couple.CoupleContext;
import com.pairflow.common.error.ApiException;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaStorageService storage;
    private final PhotoRepository photoRepository;
    private final CoupleContext coupleContext;

    public MediaController(MediaStorageService storage, PhotoRepository photoRepository, CoupleContext coupleContext) {
        this.storage = storage;
        this.photoRepository = photoRepository;
        this.coupleContext = coupleContext;
    }

    /** Auth-gated binary access. If the file backs a photo, enforce couple membership. */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable String filename) {
        photoRepository.findByStorageKey(filename).ifPresent(photo -> {
            if (!photo.getCoupleId().equals(coupleContext.requireCoupleId())) {
                throw ApiException.notFound("File not found");
            }
        });
        Resource resource = storage.loadAsResource(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storage.contentType(filename)))
                .body(resource);
    }
}
