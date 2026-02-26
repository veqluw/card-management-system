package test.task.bankcards.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import test.task.bankcards.config.SecurityConfig;
import test.task.bankcards.dto.request.UpdateUserDto;
import test.task.bankcards.dto.request.ChangePasswordRequest;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.security.JwtAuthFilter;
import test.task.bankcards.security.JwtProvider;
import test.task.bankcards.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {UserController.class})
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserResponse(
                1L,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john@example.com",
                null
        );
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void getCurrentUser_success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/me"))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUser.id()));
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void updateCurrentUser_success() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto(
                "Jane", "Smith", LocalDate.of(1995, 5, 5), "jane@example.com"
        );

        UserResponse updatedUser = new UserResponse(
                1L, "Jane", "Smith", LocalDate.of(1995, 5, 5), "jane@example.com", null
        );

        when(userService.updateCurrentUser(any(UpdateUserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void changePassword_success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("newStrongPass123");

        UserResponse updatedUser = new UserResponse(
                1L, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", null
        );

        when(userService.changePassword(any(ChangePasswordRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
