package com.pairflow.notification;

import com.pairflow.common.error.ApiException;
import com.pairflow.config.CurrentUser;
import com.pairflow.notification.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final PushSender pushSender;

    public NotificationService(NotificationRepository repository,
                               NotificationPreferenceRepository preferenceRepository,
                               PushSender pushSender) {
        this.repository = repository;
        this.preferenceRepository = preferenceRepository;
        this.pushSender = pushSender;
    }

    /**
     * Fire-and-forget notification used by other modules. Respects the recipient's
     * per-type preference and never throws — a notification failure must not break
     * the core action that triggered it.
     */
    @Transactional
    public void notify(String coupleId, String recipientId, NotificationType type,
                       String title, String body, String relatedType, String relatedId) {
        try {
            if (!isEnabled(recipientId, type)) {
                return;
            }
            Notification n = new Notification();
            n.setCoupleId(coupleId);
            n.setRecipientId(recipientId);
            n.setType(type);
            n.setTitle(title);
            n.setBody(body);
            n.setRelatedType(relatedType);
            n.setRelatedId(relatedId);
            repository.save(n);
            pushSender.send(recipientId, title, body);
        } catch (Exception e) {
            log.warn("Failed to deliver {} notification to {}: {}", type, recipientId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list() {
        return repository.findByRecipientIdOrderByCreatedAtDesc(CurrentUser.id(), PageRequest.of(0, 100))
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return repository.countByRecipientIdAndReadFalse(CurrentUser.id());
    }

    @Transactional
    public NotificationResponse markRead(String id) {
        String me = CurrentUser.id();
        Notification n = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!n.getRecipientId().equals(me)) {
            throw ApiException.notFound("Notification not found");
        }
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(Instant.now());
        }
        return NotificationResponse.from(n);
    }

    @Transactional
    public long markAllRead() {
        List<Notification> unread = repository.findByRecipientIdAndReadFalse(CurrentUser.id());
        Instant now = Instant.now();
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(now);
        });
        return unread.size();
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> getPreferences() {
        Set<NotificationType> disabled = disabledFor(CurrentUser.id());
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (NotificationType type : NotificationType.values()) {
            result.put(type.name(), !disabled.contains(type));
        }
        return result;
    }

    @Transactional
    public Map<String, Boolean> updatePreferences(List<NotificationType> disabledTypes) {
        String me = CurrentUser.id();
        NotificationPreference pref = preferenceRepository.findByUserId(me)
                .orElseGet(NotificationPreference::new);
        pref.setUserId(me);
        pref.setDisabledTypesCsv(disabledTypes == null || disabledTypes.isEmpty() ? null
                : disabledTypes.stream().distinct().map(Enum::name).collect(Collectors.joining(",")));
        preferenceRepository.save(pref);
        return getPreferences();
    }

    // ---- helpers ---------------------------------------------------------

    private boolean isEnabled(String userId, NotificationType type) {
        return !disabledFor(userId).contains(type);
    }

    private Set<NotificationType> disabledFor(String userId) {
        return preferenceRepository.findByUserId(userId)
                .map(p -> parseDisabled(p.getDisabledTypesCsv()))
                .orElseGet(() -> EnumSet.noneOf(NotificationType.class));
    }

    private Set<NotificationType> parseDisabled(String csv) {
        if (csv == null || csv.isBlank()) {
            return EnumSet.noneOf(NotificationType.class);
        }
        Set<NotificationType> set = EnumSet.noneOf(NotificationType.class);
        for (String name : Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()) {
            try {
                set.add(NotificationType.valueOf(name));
            } catch (IllegalArgumentException ignored) {
                // skip unknown/legacy type names
            }
        }
        return set;
    }
}
