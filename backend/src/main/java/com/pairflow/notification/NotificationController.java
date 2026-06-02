package com.pairflow.notification;

import com.pairflow.notification.dto.NotificationResponse;
import com.pairflow.notification.dto.UpdatePreferenceRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> list() {
        return service.list();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", service.unreadCount());
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable String id) {
        return service.markRead(id);
    }

    @PostMapping("/read-all")
    public Map<String, Long> markAllRead() {
        return Map.of("markedRead", service.markAllRead());
    }

    @GetMapping("/preferences")
    public Map<String, Boolean> getPreferences() {
        return service.getPreferences();
    }

    @PutMapping("/preferences")
    public Map<String, Boolean> updatePreferences(@RequestBody UpdatePreferenceRequest req) {
        return service.updatePreferences(req.disabledTypes());
    }
}
