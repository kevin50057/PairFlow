package com.pairflow.note;

import com.pairflow.common.error.ApiException;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleContext;
import com.pairflow.note.dto.CreateNoteRequest;
import com.pairflow.note.dto.NoteResponse;
import com.pairflow.notification.NotificationService;
import com.pairflow.notification.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository repository;
    private final CoupleContext coupleContext;
    private final NotificationService notificationService;

    public NoteService(NoteRepository repository, CoupleContext coupleContext,
                       NotificationService notificationService) {
        this.repository = repository;
        this.coupleContext = coupleContext;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> list() {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Instant now = Instant.now();
        return repository.findVisible(couple.getId(), me, now).stream()
                .map(n -> NoteResponse.from(n, now)).toList();
    }

    @Transactional
    public NoteResponse create(CreateNoteRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);

        Note n = new Note();
        n.setCoupleId(couple.getId());
        n.setSenderId(me);
        n.setReceiverId(partnerId);
        n.setTitle(req.title());
        n.setContent(req.content());
        n.setNoteType(req.noteType() != null ? req.noteType() : NoteType.NOTE);
        n.setBackgroundStyle(req.backgroundStyle());
        n.setImageUrl(req.imageUrl());
        n.setUnlockTime(req.unlockTime());
        Note saved = repository.save(n);
        if (saved.getUnlockTime() == null) {
            notificationService.notify(couple.getId(), partnerId, NotificationType.NOTE,
                    "新的小紙條", "對方寫了一張小紙條給你", "NOTE", saved.getId());
        }
        return NoteResponse.from(saved, Instant.now());
    }

    @Transactional(readOnly = true)
    public NoteResponse get(String id) {
        Instant now = Instant.now();
        return NoteResponse.from(loadVisible(id, now), now);
    }

    @Transactional
    public NoteResponse markRead(String id) {
        Instant now = Instant.now();
        Note n = loadVisible(id, now);
        String me = coupleContext.currentUserId();
        if (!n.getReceiverId().equals(me)) {
            throw ApiException.forbidden("Only the receiver can mark a note as read");
        }
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(now);
        }
        return NoteResponse.from(n, now);
    }

    @Transactional
    public NoteResponse toggleFavorite(String id) {
        Instant now = Instant.now();
        Note n = loadVisible(id, now);
        n.setFavorite(!n.isFavorite());
        return NoteResponse.from(n, now);
    }

    // ---- helpers ---------------------------------------------------------

    /** Enforces couple scope + timed-unlock visibility (404 if not yet unlocked for a receiver). */
    private Note loadVisible(String id, Instant now) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Note n = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Note not found"));
        if (!n.getCoupleId().equals(couple.getId())) {
            throw ApiException.notFound("Note not found");
        }
        boolean isSender = n.getSenderId().equals(me);
        boolean unlocked = n.getUnlockTime() == null || !n.getUnlockTime().isAfter(now);
        if (!isSender && !unlocked) {
            throw ApiException.notFound("Note not found");
        }
        return n;
    }
}
