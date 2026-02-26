package test.task.bankcards.dto.request;

import test.task.bankcards.util.enums.CardStatus;

import java.util.List;

public record CardFilter(
        Long fromEpochMillis,
        Long toEpochMillis,
        List<String> holders,
        List<CardStatus> statuses,
        List<Long> userIds
) {}
