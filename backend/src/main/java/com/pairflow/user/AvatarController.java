package com.pairflow.user;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Public avatar binary access. Avatars are intentionally public (so plain
 * {@code <img src>} works without an auth header) and live in their own storage
 * directory, isolated from private couple photos.
 */
@RestController
@RequestMapping("/api/avatars")
public class AvatarController {

    private final AvatarStorageService storage;

    public AvatarController(AvatarStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable String filename) {
        Resource resource = storage.loadAsResource(filename);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .contentType(MediaType.parseMediaType(storage.contentType(filename)))
                .body(resource);
    }
}
