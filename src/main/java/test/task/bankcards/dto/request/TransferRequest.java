package test.task.bankcards.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "From card id cannot be null")
        Long fromCardId,
        @NotNull(message = "To card id cannot be null")
        Long toCardId,
        @NotNull(message = "Amount cannot be null")
        BigDecimal amount
) {
}
