package com.pairflow.todo;

import com.pairflow.common.enums.Priority;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/** Composable, null-safe filters for the Todo list query (spec 12.3). */
public final class TodoSpecifications {

    private TodoSpecifications() {
    }

    /**
     * Couple scope + surprise-task visibility: a secret todo is only visible to its
     * creator until its unlock time passes (spec 7.3 #6 / 9.2 conditional visibility).
     */
    public static Specification<Todo> visibleTo(String coupleId, String viewerId, Instant now) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("coupleId"), coupleId),
                cb.or(
                        cb.isFalse(root.get("secret")),
                        cb.equal(root.get("createdBy"), viewerId),
                        cb.and(
                                cb.isNotNull(root.get("secretUnlockAt")),
                                cb.lessThanOrEqualTo(root.get("secretUnlockAt"), now))));
    }

    public static Specification<Todo> status(TodoStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Todo> type(TodoType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<Todo> priority(Priority priority) {
        return (root, query, cb) -> priority == null ? cb.conjunction() : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Todo> dueFrom(Instant from) {
        return (root, query, cb) -> from == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("dueDate"), from);
    }

    public static Specification<Todo> dueTo(Instant to) {
        return (root, query, cb) -> to == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("dueDate"), to);
    }

    public static Specification<Todo> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like));
        };
    }

    public static Specification<Todo> assignee(AssigneeOption option, String viewerId, String partnerId) {
        if (option == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return switch (option) {
            case ME -> (root, query, cb) -> cb.equal(root.get("assigneeUserId"), viewerId);
            case PARTNER -> (root, query, cb) -> cb.equal(root.get("assigneeUserId"), partnerId);
            case BOTH -> (root, query, cb) -> cb.isTrue(root.get("assignedToBoth"));
            case UNASSIGNED -> (root, query, cb) -> cb.and(
                    cb.isFalse(root.get("assignedToBoth")), cb.isNull(root.get("assigneeUserId")));
        };
    }
}
