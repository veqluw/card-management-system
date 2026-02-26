package test.task.bankcards.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long accessExpiresInMs,
        long refreshExpiresInMs
){}
