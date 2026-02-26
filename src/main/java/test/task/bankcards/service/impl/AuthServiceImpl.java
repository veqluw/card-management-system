package test.task.bankcards.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import test.task.bankcards.dto.request.AuthRequest;
import test.task.bankcards.dto.request.RefreshRequest;
import test.task.bankcards.dto.response.AuthResponse;
import test.task.bankcards.entity.RefreshToken;
import test.task.bankcards.entity.User;
import test.task.bankcards.exception.InvalidCredentialsException;
import test.task.bankcards.exception.RefreshTokenExpiredException;
import test.task.bankcards.exception.RefreshTokenNotFoundException;
import test.task.bankcards.exception.UserNotFoundException;
import test.task.bankcards.reposiory.RefreshTokenRepository;
import test.task.bankcards.reposiory.UserRepository;
import test.task.bankcards.security.JwtProvider;
import test.task.bankcards.service.AuthService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String access = jwtProvider.generateAccessToken(user.getId(), user.getRole().getType());
        String refresh = jwtProvider.generateRefreshToken(user.getId());

        persistRefreshToken(refresh, user.getId());

        return new AuthResponse(access, refresh, jwtProvider.getAccessTtlMs(), jwtProvider.getRefreshTtlMs());
    }

    @Transactional
    @Override
    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        Jws<Claims> parsed;

        try {
            parsed = jwtProvider.validateToken(token);
        } catch (Exception e) {
            throw new InvalidCredentialsException();
        }

        Claims claims = parsed.getBody();
        if (!"refresh".equals(claims.get("type"))) {
            throw new InvalidCredentialsException();
        }

        String jti = claims.getId();

        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException();
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new UserNotFoundException(stored.getUserId()));


        String newAccess = jwtProvider.generateAccessToken(user.getId(), user.getRole().getType());
        String newRefresh = jwtProvider.generateRefreshToken(user.getId());

        persistRefreshToken(newRefresh, user.getId());

        return new AuthResponse(newAccess, newRefresh, jwtProvider.getAccessTtlMs(), jwtProvider.getRefreshTtlMs());
    }

    @Transactional
    @Override
    public void revokeRefreshToken(String jti) {
        RefreshToken token = refreshTokenRepository.findByJti(jti)
                .orElseThrow(RefreshTokenNotFoundException::new);
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Override
    public Jws<Claims> validateToken(String token) {
        return jwtProvider.validateToken(token);
    }

    @Transactional
    @Override
    public void logout(String refreshToken) {
        String hash = hashToken(refreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(RefreshTokenNotFoundException::new);

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
    }

    private void persistRefreshToken(String rawToken, Long userId) {
        Jws<Claims> parsed = jwtProvider.parse(rawToken);

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setJti(parsed.getBody().getId());
        token.setTokenHash(hashToken(rawToken));
        token.setIssuedAt(parsed.getBody().getIssuedAt().toInstant());
        token.setExpiresAt(parsed.getBody().getExpiration().toInstant());
        token.setRevoked(false);

        refreshTokenRepository.save(token);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        }
    }

}
