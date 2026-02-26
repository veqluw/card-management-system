package test.task.bankcards.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import test.task.bankcards.dto.request.AuthRequest;
import test.task.bankcards.dto.request.RefreshRequest;
import test.task.bankcards.dto.response.AuthResponse;
import test.task.bankcards.entity.RefreshToken;
import test.task.bankcards.entity.Role;
import test.task.bankcards.entity.User;
import test.task.bankcards.exception.InvalidCredentialsException;
import test.task.bankcards.exception.RefreshTokenExpiredException;
import test.task.bankcards.reposiory.RefreshTokenRepository;
import test.task.bankcards.reposiory.UserRepository;
import test.task.bankcards.security.JwtProvider;
import test.task.bankcards.service.impl.AuthServiceImpl;
import test.task.bankcards.util.enums.RoleType;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {

        AuthRequest request = new AuthRequest("test@mail.com", "password");

        User user = new User();
        user.setId(1L);
        user.setPasswordHash("hashed");
        Role role = new Role();
        role.setType(RoleType.USER);
        user.setRole(role);

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "hashed"))
                .thenReturn(true);

        when(jwtProvider.generateAccessToken(1L, RoleType.USER))
                .thenReturn("access-token");

        when(jwtProvider.generateRefreshToken(1L))
                .thenReturn("refresh-token");

        when(jwtProvider.getAccessTtlMs()).thenReturn(1000L);
        when(jwtProvider.getRefreshTtlMs()).thenReturn(2000L);

        mockPersistRefreshToken("refresh-token", 1L);

        AuthResponse response = authService.login(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void login_shouldThrow_whenPasswordInvalid() {

        AuthRequest request = new AuthRequest("test@mail.com", "wrong");

        User user = new User();
        user.setPasswordHash("hashed");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "hashed"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {

        when(userRepository.findByEmail("no@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new AuthRequest("no@mail.com", "123")));
    }

    @Test
    void refresh_shouldGenerateNewTokens_whenValid() {

        String refreshToken = "refresh-token";

        Claims claims = mock(Claims.class);
        when(claims.get("type")).thenReturn("refresh");
        when(claims.getId()).thenReturn("jti-123");

        Jws<Claims> jws = mock(Jws.class);
        when(jws.getBody()).thenReturn(claims);

        when(jwtProvider.validateToken(refreshToken)).thenReturn(jws);

        RefreshToken stored = new RefreshToken();
        stored.setUserId(1L);
        stored.setJti("jti-123");
        stored.setRevoked(false);
        stored.setExpiresAt(Instant.now().plusSeconds(1000));

        when(refreshTokenRepository.findByJti("jti-123"))
                .thenReturn(Optional.of(stored));

        User user = new User();
        user.setId(1L);
        Role role = new Role();
        role.setType(RoleType.USER);
        user.setRole(role);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(jwtProvider.generateAccessToken(1L, RoleType.USER))
                .thenReturn("new-access");

        when(jwtProvider.generateRefreshToken(1L))
                .thenReturn("new-refresh");

        when(jwtProvider.getAccessTtlMs()).thenReturn(1000L);
        when(jwtProvider.getRefreshTtlMs()).thenReturn(2000L);

        mockPersistRefreshToken("new-refresh", 1L);

        AuthResponse response = authService.refresh(new RefreshRequest(refreshToken));

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
    }

    @Test
    void refresh_shouldThrow_whenTokenExpired() {

        String token = "refresh";

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti");
        when(claims.get("type")).thenReturn("refresh");

        Jws<Claims> jws = mock(Jws.class);
        when(jws.getBody()).thenReturn(claims);

        when(jwtProvider.validateToken(token)).thenReturn(jws);

        RefreshToken stored = new RefreshToken();
        stored.setRevoked(false);
        stored.setExpiresAt(Instant.now().minusSeconds(10));

        when(refreshTokenRepository.findByJti("jti"))
                .thenReturn(Optional.of(stored));

        assertThrows(RefreshTokenExpiredException.class,
                () -> authService.refresh(new RefreshRequest(token)));
    }

    @Test
    void logout_shouldRevokeToken() {

        String refreshToken = "some-token";

        RefreshToken stored = new RefreshToken();
        stored.setRevoked(false);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(stored));

        authService.logout(refreshToken);

        assertTrue(stored.isRevoked());
        verify(refreshTokenRepository).save(stored);
    }

    private void mockPersistRefreshToken(String token, Long userId) {

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-123");
        when(claims.getIssuedAt()).thenReturn(new Date());
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 10000));

        Jws<Claims> jws = mock(Jws.class);
        when(jws.getBody()).thenReturn(claims);

        when(jwtProvider.parse(token)).thenReturn(jws);
    }

}
