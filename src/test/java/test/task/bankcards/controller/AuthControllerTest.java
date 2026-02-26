package test.task.bankcards.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import test.task.bankcards.dto.request.AuthRequest;
import test.task.bankcards.dto.request.RefreshRequest;
import test.task.bankcards.dto.request.CreateUserDto;
import test.task.bankcards.dto.response.AuthResponse;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.exception.GlobalExceptionHandler;
import test.task.bankcards.exception.MissingTokenException;
import test.task.bankcards.security.JwtAuthFilter;
import test.task.bankcards.service.AuthService;
import test.task.bankcards.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AuthController.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthController authController;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registration_shouldReturnCreated() throws Exception {
        CreateUserDto dto = new CreateUserDto(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john@mail.com",
                "password123"
        );

        UserResponse response = new UserResponse(
                1L,
                "John",
                "Doe",
                LocalDate.of(1990,1,1),
                "john@mail.com",
                null
        );

        when(userService.createUser(any(CreateUserDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/registration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"));
    }

    @Test
    void login_shouldReturnAuthResponse() throws Exception {
        AuthRequest request = new AuthRequest("john@mail.com", "password123");
        AuthResponse response = new AuthResponse("access-token", "refresh-token", 3600000, 86400000);

        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void refresh_shouldReturnNewAuthTokens() throws Exception {
        RefreshRequest request = new RefreshRequest("old-refresh-token");
        AuthResponse response = new AuthResponse("new-access-token", "new-refresh-token", 3600000, 86400000);

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }



    @Test
    void validate_withValidToken_returnsClaims() throws Exception {
        String token = "valid.jwt.token";

        DefaultClaims claims = new DefaultClaims();
        claims.put("user", "misha");
        claims.setSubject("1");
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60));

        Jws<Claims> jws = Mockito.mock(Jws.class);
        Mockito.when(jws.getBody()).thenReturn(claims);

        Mockito.when(authService.validateToken(token)).thenReturn(jws);

        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("misha"));
    }

    @Test
    void validate_shouldReturnClaims_whenTokenPresent() throws Exception {
        String token = "dummy-token";

        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);

        when(authService.validateToken(token)).thenReturn(jws);
        when(jws.getBody()).thenReturn(claims);

        Map<String, Object> claimsMap = Map.of("sub", "1", "role", "ADMIN");
        when(claims.entrySet()).thenReturn(claimsMap.entrySet());

        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub").value("1"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void validate_invalidTokenFormat_throwsException() throws Exception {
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "InvalidToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof MissingTokenException)
                );
    }

    @Test
    void logout_shouldReturnNoContent() throws Exception {
        RefreshRequest request = new RefreshRequest("some-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}