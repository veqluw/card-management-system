package test.task.bankcards.util.specification;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import test.task.bankcards.entity.Card;
import test.task.bankcards.entity.User;

import java.time.Instant;
import java.util.List;

public class UserSpecification {

    public static Specification<User> createdBetween(Instant from, Instant to) {
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

    public static Specification<User> hasName(List<String> names) {
        return (root, query, cb) -> {
            if (names == null || names.isEmpty()) return cb.conjunction();
            return root.get("name").in(names);
        };
    }

    public static Specification<User> hasSurname(List<String> surnames) {
        return (root, query, cb) -> {
            if (surnames == null || surnames.isEmpty()) return cb.conjunction();
            return root.get("surname").in(surnames);
        };
    }
}
