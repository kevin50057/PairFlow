package com.pairflow.notification;

import com.pairflow.anniversary.Anniversary;
import com.pairflow.anniversary.AnniversaryRepository;
import com.pairflow.anniversary.AnniversaryService;
import com.pairflow.anniversary.RepeatType;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleRepository;
import com.pairflow.couple.CoupleStatus;
import com.pairflow.todo.Todo;
import com.pairflow.todo.TodoRepository;
import com.pairflow.todo.TodoStatus;
import com.pairflow.user.User;
import com.pairflow.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Daily notification dispatch for anniversaries and todo due dates (spec 10, 7.4).
 * Runs at 08:00 server time. Both anniversary and todo scans iterate over ACTIVE
 * couples only, so the load stays proportional to active users.
 */
@Slf4j
@Component
public class NotificationScheduler {

    private final CoupleRepository coupleRepository;
    private final AnniversaryRepository anniversaryRepository;
    private final TodoRepository todoRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /** Reminder offsets (days before) for birthdays. */
    private static final long[] BIRTHDAY_OFFSETS = {7, 1, 0};

    public NotificationScheduler(CoupleRepository coupleRepository,
                                 AnniversaryRepository anniversaryRepository,
                                 TodoRepository todoRepository,
                                 NotificationService notificationService,
                                 UserRepository userRepository) {
        this.coupleRepository = coupleRepository;
        this.anniversaryRepository = anniversaryRepository;
        this.todoRepository = todoRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    /** Birthday reminders for both members of each active couple (spec 7.4). */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendBirthdayReminders() {
        LocalDate today = AppTime.today();
        int sent = 0;
        for (Couple couple : coupleRepository.findAllByStatus(CoupleStatus.ACTIVE)) {
            for (String userId : List.of(couple.getUserAId(), couple.getUserBId())) {
                User u = userRepository.findById(userId).orElse(null);
                if (u == null || u.getBirthday() == null) continue;
                MonthDay md = MonthDay.from(u.getBirthday());
                LocalDate next = md.atYear(today.getYear());
                if (next.isBefore(today)) next = md.atYear(today.getYear() + 1);
                long daysLeft = ChronoUnit.DAYS.between(today, next);
                if (Arrays.stream(BIRTHDAY_OFFSETS).noneMatch(d -> d == daysLeft)) continue;
                String title = u.getDisplayName() + " 生日";
                String msg = daysLeft == 0
                        ? "今天是 " + u.getDisplayName() + " 的生日 🎂"
                        : "距離 " + u.getDisplayName() + " 的生日還有 " + daysLeft + " 天 🎂";
                notifyBoth(couple, NotificationType.ANNIVERSARY, title, msg, "BIRTHDAY", u.getId());
                sent++;
            }
        }
        log.info("[scheduler] birthday reminders sent: {}", sent);
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendAnniversaryReminders() {
        LocalDate today = AppTime.today();
        List<Couple> active = coupleRepository.findAllByStatus(CoupleStatus.ACTIVE);
        int sent = 0;
        for (Couple couple : active) {
            for (Anniversary ann : anniversaryRepository.findByCoupleId(couple.getId())) {
                LocalDate next = AnniversaryService.nextOccurrence(ann.getDate(), ann.getRepeatType());
                long daysLeft = ChronoUnit.DAYS.between(today, next);
                if (reminderEnabled(ann.getReminderDaysBeforeCsv(), daysLeft)) {
                    String msg = daysLeft == 0
                            ? "今天是「" + ann.getTitle() + "」🎉"
                            : "距離「" + ann.getTitle() + "」還有 " + daysLeft + " 天";
                    notifyBoth(couple, NotificationType.ANNIVERSARY, ann.getTitle(), msg, "ANNIVERSARY", ann.getId());
                    sent++;
                }
            }
        }
        log.info("[scheduler] anniversary reminders sent: {}", sent);
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendTodoDueReminders() {
        Instant from = Instant.now();
        Instant to = from.plus(24, ChronoUnit.HOURS);
        List<Todo> dueSoon = todoRepository.findByStatusAndDueDateBetween(
                TodoStatus.PENDING, from, to);
        for (Todo todo : dueSoon) {
            if (todo.getReminderTime() == null || todo.getReminderTime().isAfter(to)) {
                continue; // no reminder configured for this window
            }
            Couple couple = coupleRepository.findById(todo.getCoupleId()).orElse(null);
            if (couple == null) continue;
            String assignee = todo.getAssigneeUserId();
            String title = "任務即將到期：" + todo.getTitle();
            String body = "記得完成「" + todo.getTitle() + "」";
            if (assignee != null) {
                notificationService.notify(todo.getCoupleId(), assignee,
                        NotificationType.TODO_DUE, title, body, "TODO", todo.getId());
            } else {
                notifyBoth(couple, NotificationType.TODO_DUE, title, body, "TODO", todo.getId());
            }
        }
        log.info("[scheduler] todo due reminders: {} todos checked", dueSoon.size());
    }

    /**
     * Auto-complete todos that are bound to the calendar (autoComplete=true) once their
     * due time passes. Runs every minute so completion feels prompt.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void autoCompleteDueTodos() {
        List<Todo> due = todoRepository.findByStatusAndAutoCompleteTrueAndDueDateLessThanEqual(
                TodoStatus.PENDING, Instant.now());
        if (due.isEmpty()) return;
        for (Todo todo : due) {
            todo.setStatus(TodoStatus.DONE);
            todo.setCompletedAt(Instant.now());
            Couple couple = coupleRepository.findById(todo.getCoupleId()).orElse(null);
            if (couple != null) {
                notifyBoth(couple, NotificationType.TODO_COMPLETED, "任務已自動完成",
                        "「" + todo.getTitle() + "」時間到，已自動標記完成 ✓", "TODO", todo.getId());
            }
        }
        todoRepository.saveAll(due);
        log.info("[scheduler] auto-completed {} due todos", due.size());
    }

    private void notifyBoth(Couple couple, NotificationType type,
                            String title, String body, String relatedType, String relatedId) {
        notificationService.notify(couple.getId(), couple.getUserAId(), type, title, body, relatedType, relatedId);
        notificationService.notify(couple.getId(), couple.getUserBId(), type, title, body, relatedType, relatedId);
    }

    private boolean reminderEnabled(String csv, long daysLeft) {
        if (csv == null || csv.isBlank()) return false;
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToLong(Long::parseLong)
                .anyMatch(d -> d == daysLeft);
    }
}
