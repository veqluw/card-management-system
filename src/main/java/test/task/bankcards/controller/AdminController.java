package test.task.bankcards.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import test.task.bankcards.dto.request.UserFilter;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<Page<UserResponse>> getAllUsers (
            @ModelAttribute UserFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                userService.getAll(filter, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getOneById(@PathVariable Long id) {
        return ResponseEntity.ok(
                userService.getOneById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

}
