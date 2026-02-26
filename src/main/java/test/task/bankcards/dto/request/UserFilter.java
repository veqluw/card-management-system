package test.task.bankcards.dto.request;

import java.util.List;

public record UserFilter(
        Long fromEpochMillis,
        Long toEpochMillis,
        List<String> names,
        List<String> surnames
) {
}
