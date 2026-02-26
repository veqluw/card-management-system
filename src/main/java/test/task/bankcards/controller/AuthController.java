package test.task.bankcards.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import test.task.bankcards.dto.request.AuthRequest;
import test.task.bankcards.dto.request.CreateUserDto;
import test.task.bankcards.dto.request.RefreshRequest;
import test.task.bankcards.dto.response.AuthResponse;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.exception.MissingTokenException;
import test.task.bankcards.service.AuthService;
import test.task.bankcards.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/registration")
    public ResponseEntity<UserResponse> registration(@RequestBody @Valid CreateUserDto createUserDto) {
        UserResponse userResponse = userService.createUser(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        AuthResponse resp = authService.refresh(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MissingTokenException("Missing Bearer token");
        }
        String token = authHeader.substring(7);
        var claims = authService.validateToken(token);

        return ResponseEntity.ok(claims.getBody());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

}
