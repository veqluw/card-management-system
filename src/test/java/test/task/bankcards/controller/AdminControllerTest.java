package test.task.bankcards.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import test.task.bankcards.config.SecurityConfig;
import test.task.bankcards.dto.request.UserFilter;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.security.JwtAuthFilter;
import test.task.bankcards.security.JwtProvider;
import test.task.bankcards.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@ContextConfiguration(classes = {AdminController.class})
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse mockUser;
    private UserResponse user1;
    private UserResponse user2;

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

        user1 = new UserResponse(
                1L, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", null
        );
        user2 = new UserResponse(
                2L, "Jane", "Smith", LocalDate.of(1995, 5, 5), "jane@example.com", null
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_success() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(user1, user2));

        when(userService.getAll(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(user1.id()))
                .andExpect(jsonPath("$.content[0].name").value(user1.name()))
                .andExpect(jsonPath("$.content[1].id").value(user2.id()))
                .andExpect(jsonPath("$.content[1].name").value(user2.name()));

        verify(userService).getAll(any(), any());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getOneById_success() throws Exception {
        when(userService.getOneById(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUser.id()))
                .andExpect(jsonPath("$.email").value(mockUser.email()));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteOneById_success() throws Exception {
        doNothing().when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());
    }
}

