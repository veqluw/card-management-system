package test.task.bankcards.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import test.task.bankcards.dto.request.AuthRequest;
import test.task.bankcards.dto.request.RefreshRequest;
import test.task.bankcards.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);
    AuthResponse refresh(RefreshRequest request);
    void revokeRefreshToken(String jti);
    Jws<Claims> validateToken(String token);
    void logout(String refreshToken);

}
