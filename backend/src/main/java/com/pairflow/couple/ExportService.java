package com.pairflow.couple;

import com.pairflow.anniversary.AnniversaryRepository;
import com.pairflow.common.error.ApiException;
import com.pairflow.common.util.AppTime;
import com.pairflow.event.EventRepository;
import com.pairflow.note.NoteRepository;
import com.pairflow.album.PhotoRepository;
import com.pairflow.todo.TodoRepository;
import com.pairflow.wishlist.WishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Produces a portable JSON export of all couple data for a requesting member (spec 9.3). */
@Service
public class ExportService {

    private final CoupleRepository coupleRepository;
    private final TodoRepository todoRepository;
    private final EventRepository eventRepository;
    private final AnniversaryRepository anniversaryRepository;
    private final NoteRepository noteRepository;
    private final PhotoRepository photoRepository;
    private final WishRepository wishRepository;

    public ExportService(CoupleRepository coupleRepository,
                         TodoRepository todoRepository,
                         EventRepository eventRepository,
                         AnniversaryRepository anniversaryRepository,
                         NoteRepository noteRepository,
                         PhotoRepository photoRepository,
                         WishRepository wishRepository) {
        this.coupleRepository = coupleRepository;
        this.todoRepository = todoRepository;
        this.eventRepository = eventRepository;
        this.anniversaryRepository = anniversaryRepository;
        this.noteRepository = noteRepository;
        this.photoRepository = photoRepository;
        this.wishRepository = wishRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> export(String coupleId, String requesterId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> ApiException.notFound("Couple not found"));
        if (!couple.hasMember(requesterId)) {
            throw ApiException.forbidden("Not a member of this couple");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("exportedAt", Instant.now().toString());
        out.put("exportedBy", requesterId);

        // Couple metadata
        LocalDate start = couple.getRelationshipStartDate();
        Long days = start != null ? ChronoUnit.DAYS.between(start, AppTime.today()) + 1 : null;
        out.put("couple", Map.of(
                "id", coupleId,
                "relationshipStartDate", start != null ? start.toString() : "",
                "daysTogether", days != null ? days : 0,
                "status", couple.getStatus().name(),
                "createdAt", couple.getCreatedAt().toString()
        ));

        // Todos
        out.put("todos", todoRepository.findAll().stream()
                .filter(t -> t.getCoupleId().equals(coupleId))
                .map(t -> Map.of(
                        "id", t.getId(),
                        "title", t.getTitle(),
                        "type", t.getType().name(),
                        "status", t.getStatus().name(),
                        "priority", t.getPriority().name(),
                        "dueDate", t.getDueDate() != null ? t.getDueDate().toString() : "",
                        "completedAt", t.getCompletedAt() != null ? t.getCompletedAt().toString() : "",
                        "createdAt", t.getCreatedAt().toString()
                ))
                .collect(Collectors.toList()));

        // Events
        out.put("events", eventRepository.findByCoupleIdOrderByStartTimeAsc(coupleId).stream()
                .map(e -> Map.of(
                        "id", e.getId(),
                        "title", e.getTitle(),
                        "eventType", e.getEventType().name(),
                        "startTime", e.getStartTime().toString(),
                        "locationName", e.getLocationName() != null ? e.getLocationName() : "",
                        "createdAt", e.getCreatedAt().toString()
                ))
                .collect(Collectors.toList()));

        // Anniversaries
        out.put("anniversaries", anniversaryRepository.findByCoupleId(coupleId).stream()
                .map(a -> Map.of(
                        "id", a.getId(),
                        "title", a.getTitle(),
                        "date", a.getDate().toString(),
                        "repeatType", a.getRepeatType().name(),
                        "createdAt", a.getCreatedAt().toString()
                ))
                .collect(Collectors.toList()));

        // Notes sent by or received by this user (unlocked)
        out.put("notes", noteRepository.findVisible(coupleId, requesterId, Instant.now()).stream()
                .map(n -> Map.of(
                        "id", n.getId(),
                        "title", n.getTitle() != null ? n.getTitle() : "",
                        "content", n.getContent(),
                        "noteType", n.getNoteType().name(),
                        "senderId", n.getSenderId(),
                        "receiverId", n.getReceiverId(),
                        "createdAt", n.getCreatedAt().toString()
                ))
                .collect(Collectors.toList()));

        // Photo metadata (no binary; images can be retrieved via /api/media/{storageKey})
        out.put("photos", photoRepository.findByCoupleId(coupleId).stream()
                .map(p -> Map.of(
                        "id", p.getId(),
                        "storageKey", p.getStorageKey(),
                        "caption", p.getCaption() != null ? p.getCaption() : "",
                        "takenAt", p.getTakenAt() != null ? p.getTakenAt().toString() : "",
                        "locationName", p.getLocationName() != null ? p.getLocationName() : "",
                        "createdAt", p.getCreatedAt().toString()
                ))
                .collect(Collectors.toList()));

        // Wishes
        out.put("wishes", wishRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId).stream()
                .map(w -> Map.of(
                        "id", w.getId(),
                        "title", w.getTitle(),
                        "category", w.getCategory().name(),
                        "status", w.getStatus().name(),
                        "createdAt", w.getCreatedAt().toString()
                ))
                .collect(Collectors.toList()));

        return out;
    }
}
