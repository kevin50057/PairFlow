package com.pairflow.event;

import com.pairflow.common.error.ApiException;
import com.pairflow.couple.CoupleContext;
import com.pairflow.event.dto.CreateEventRequest;
import com.pairflow.event.dto.EventResponse;
import com.pairflow.event.dto.UpdateEventRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class EventService {

    private final EventRepository repository;
    private final CoupleContext coupleContext;

    public EventService(EventRepository repository, CoupleContext coupleContext) {
        this.repository = repository;
        this.coupleContext = coupleContext;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list(Instant from, Instant to) {
        String coupleId = coupleContext.requireCoupleId();
        List<Event> events = (from != null && to != null)
                ? repository.findByCoupleIdAndStartTimeBetweenOrderByStartTimeAsc(coupleId, from, to)
                : repository.findByCoupleIdOrderByStartTimeAsc(coupleId);
        return events.stream().map(EventResponse::from).toList();
    }

    /** Events on a given day [from, to) — used by the home dashboard. */
    @Transactional(readOnly = true)
    public List<EventResponse> between(String coupleId, Instant from, Instant to) {
        return repository.findByCoupleIdAndStartTimeBetweenOrderByStartTimeAsc(coupleId, from, to)
                .stream().map(EventResponse::from).toList();
    }

    @Transactional
    public EventResponse create(CreateEventRequest req) {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();
        Event e = new Event();
        e.setCoupleId(coupleId);
        e.setCreatedBy(me);
        apply(e, req.title(), req.description(), req.eventType(), req.startTime(), req.endTime(),
                req.locationName(), req.locationAddress(), req.reminderTime(), req.budget(),
                req.transport(), req.dressCode(), req.reservationInfo(), req.relatedTodoId(), req.relatedAlbumId());
        if (e.getEventType() == null) e.setEventType(EventType.DATE);
        return EventResponse.from(repository.save(e));
    }

    @Transactional(readOnly = true)
    public EventResponse get(String id) {
        return EventResponse.from(load(id));
    }

    @Transactional
    public EventResponse update(String id, UpdateEventRequest req) {
        Event e = load(id);
        if (req.title() != null) e.setTitle(req.title().trim());
        if (req.description() != null) e.setDescription(req.description());
        if (req.eventType() != null) e.setEventType(req.eventType());
        if (req.startTime() != null) e.setStartTime(req.startTime());
        if (req.endTime() != null) e.setEndTime(req.endTime());
        if (req.locationName() != null) e.setLocationName(req.locationName());
        if (req.locationAddress() != null) e.setLocationAddress(req.locationAddress());
        if (req.reminderTime() != null) e.setReminderTime(req.reminderTime());
        if (req.budget() != null) e.setBudget(req.budget());
        if (req.transport() != null) e.setTransport(req.transport());
        if (req.dressCode() != null) e.setDressCode(req.dressCode());
        if (req.reservationInfo() != null) e.setReservationInfo(req.reservationInfo());
        if (req.relatedTodoId() != null) e.setRelatedTodoId(req.relatedTodoId());
        if (req.relatedAlbumId() != null) e.setRelatedAlbumId(req.relatedAlbumId());
        return EventResponse.from(e);
    }

    @Transactional
    public void delete(String id) {
        repository.delete(load(id));
    }

    private Event load(String id) {
        Event e = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Event not found"));
        if (!e.getCoupleId().equals(coupleContext.requireCoupleId())) {
            throw ApiException.notFound("Event not found");
        }
        return e;
    }

    private void apply(Event e, String title, String description, EventType type, Instant start, Instant end,
                       String locationName, String locationAddress, Instant reminder, Double budget,
                       String transport, String dressCode, String reservationInfo,
                       String relatedTodoId, String relatedAlbumId) {
        e.setTitle(title.trim());
        e.setDescription(description);
        e.setEventType(type);
        e.setStartTime(start);
        e.setEndTime(end);
        e.setLocationName(locationName);
        e.setLocationAddress(locationAddress);
        e.setReminderTime(reminder);
        e.setBudget(budget);
        e.setTransport(transport);
        e.setDressCode(dressCode);
        e.setReservationInfo(reservationInfo);
        e.setRelatedTodoId(relatedTodoId);
        e.setRelatedAlbumId(relatedAlbumId);
    }
}
