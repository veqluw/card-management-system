package test.task.bankcards.util.specification;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import test.task.bankcards.entity.Card;
import test.task.bankcards.util.enums.CardStatus;

import java.time.Instant;
import java.util.List;

public class CardSpecification {

    public static Specification<Card> createdBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            Path<Instant> createdAt = root.get("createdAt");
            if (from != null && to != null) {
                return cb.between(createdAt, from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(createdAt, from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(createdAt, to);
            } else {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Card> hasHolder(List<String> holders) {
        return (root, query, cb) -> {
            if (holders == null || holders.isEmpty()) return cb.conjunction();
            return root.get("holder").in(holders);
        };
    }

    public static Specification<Card> hasStatuses(List<CardStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return cb.conjunction();
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Card> hasUserId(List<Long> userIds) {
        return (root, query, cb) -> {
            if (userIds == null || userIds.isEmpty()) return cb.conjunction();
            return root.get("user").get("id").in(userIds);
        };
    }
}
