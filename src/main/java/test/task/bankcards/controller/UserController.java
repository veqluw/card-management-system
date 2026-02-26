package test.task.bankcards.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import test.task.bankcards.dto.request.ChangePasswordRequest;
import test.task.bankcards.dto.request.UpdateUserDto;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(
                userService.getCurrentUser()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateUserDto updateUserDto) {
        return ResponseEntity.ok(
                userService.updateCurrentUser(updateUserDto)
        );
    }

    @PutMapping("me/password")
    public ResponseEntity<UserResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(
                userService.changePassword(request)
        );
    }
}
