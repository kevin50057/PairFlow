package com.pairflow.anniversary;

import com.pairflow.anniversary.dto.AnniversaryResponse;
import com.pairflow.anniversary.dto.CreateAnniversaryRequest;
import com.pairflow.anniversary.dto.UpdateAnniversaryRequest;
import com.pairflow.common.error.ApiException;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.CoupleContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnniversaryService {

    private static final List<Integer> DEFAULT_REMINDERS = List.of(30, 7, 3, 1, 0);

    private final AnniversaryRepository repository;
    private final CoupleContext coupleContext;

    public AnniversaryService(AnniversaryRepository repository, CoupleContext coupleContext) {
        this.repository = repository;
        this.coupleContext = coupleContext;
    }

    @Transactional(readOnly = true)
    public List<AnniversaryResponse> list() {
        String coupleId = coupleContext.requireCoupleId();
        return repository.findByCoupleId(coupleId).stream()
                .map(this::toResponse)
                .sorted(Comparator.comparingLong(AnniversaryResponse::daysLeft))
                .toList();
    }

    @Transactional
    public AnniversaryResponse create(CreateAnniversaryRequest req) {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();
        Anniversary a = new Anniversary();
        a.setCoupleId(coupleId);
        a.setTitle(req.title().trim());
        a.setDate(req.date());
        a.setRepeatType(req.repeatType() != null ? req.repeatType() : RepeatType.YEARLY);
        a.setReminderDaysBeforeCsv(toCsv(req.reminderDaysBefore() != null ? req.reminderDaysBefore() : DEFAULT_REMINDERS));
        a.setDescription(req.description());
        a.setCreatedBy(me);
        return toResponse(repository.save(a));
    }

    @Transactional
    public AnniversaryResponse update(String id, UpdateAnniversaryRequest req) {
        Anniversary a = load(id);
        if (req.title() != null) a.setTitle(req.title().trim());
        if (req.date() != null) a.setDate(req.date());
        if (req.repeatType() != null) a.setRepeatType(req.repeatType());
        if (req.reminderDaysBefore() != null) a.setReminderDaysBeforeCsv(toCsv(req.reminderDaysBefore()));
        if (req.description() != null) a.setDescription(req.description());
        return toResponse(a);
    }

    @Transactional
    public void delete(String id) {
        repository.delete(load(id));
    }

    /** The soonest upcoming anniversary — used by the home dashboard (spec 22). */
    @Transactional(readOnly = true)
    public AnniversaryResponse nextUpcoming(String coupleId) {
        return repository.findByCoupleId(coupleId).stream()
                .map(this::toResponse)
                .filter(r -> r.daysLeft() >= 0)
                .min(Comparator.comparingLong(AnniversaryResponse::daysLeft))
                .orElse(null);
    }

    // ---- helpers ---------------------------------------------------------

    private Anniversary load(String id) {
        Anniversary a = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Anniversary not found"));
        if (!a.getCoupleId().equals(coupleContext.requireCoupleId())) {
            throw ApiException.notFound("Anniversary not found");
        }
        return a;
    }

    private AnniversaryResponse toResponse(Anniversary a) {
        LocalDate next = nextOccurrence(a.getDate(), a.getRepeatType());
        long daysLeft = ChronoUnit.DAYS.between(AppTime.today(), next);
        return new AnniversaryResponse(
                a.getId(), a.getCoupleId(), a.getTitle(), a.getDate(),
                a.getRepeatType().name(), parseCsv(a.getReminderDaysBeforeCsv()),
                a.getDescription(), next, daysLeft, a.getCreatedBy(), a.getCreatedAt());
    }

    /** Next occurrence on/after today for repeating dates; the date itself otherwise. */
    public static LocalDate nextOccurrence(LocalDate base, RepeatType repeatType) {
        LocalDate today = AppTime.today();
        return switch (repeatType) {
            case NONE -> base;
            case YEARLY -> {
                LocalDate candidate = base.withYear(today.getYear());
                yield candidate.isBefore(today) ? candidate.plusYears(1) : candidate;
            }
            case MONTHLY -> {
                LocalDate candidate = safeDay(today, base.getDayOfMonth());
                yield candidate.isBefore(today) ? safeDay(today.plusMonths(1), base.getDayOfMonth()) : candidate;
            }
        };
    }

    private static LocalDate safeDay(LocalDate month, int day) {
        return month.withDayOfMonth(Math.min(day, month.lengthOfMonth()));
    }

    private String toCsv(List<Integer> values) {
        return values.stream().distinct().sorted(Comparator.reverseOrder())
                .map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<Integer> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(Integer::parseInt).toList();
    }
}
