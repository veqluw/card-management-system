package test.task.bankcards.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import test.task.bankcards.dto.request.ChangePasswordRequest;
import test.task.bankcards.dto.request.CreateUserDto;
import test.task.bankcards.dto.request.UpdateUserDto;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.entity.Role;
import test.task.bankcards.entity.User;
import test.task.bankcards.exception.EmailAlreadyUsedException;
import test.task.bankcards.exception.UserNotFoundException;
import test.task.bankcards.reposiory.RoleRepository;
import test.task.bankcards.reposiory.UserRepository;
import test.task.bankcards.service.impl.UserServiceImpl;
import test.task.bankcards.util.enums.RoleType;
import test.task.bankcards.util.SecurityUtils;
import test.task.bankcards.util.mapper.UserMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getOneById_shouldReturnUser() {

        User user = new User();
        user.setId(1L);

        UserResponse response = mock(UserResponse.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(response);

        UserResponse result = userService.getOneById(1L);

        assertEquals(response, result);
    }

    @Test
    void getOneById_shouldThrow_whenNotFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getOneById(1L));
    }

    @Test
    void createUser_shouldSaveUser() {
        CreateUserDto dto = new CreateUserDto("John", "Doe", null, "mail@test.com", "pass");

        User user = new User();
        Role role = new Role();
        role.setType(RoleType.USER);

        UserResponse response = mock(UserResponse.class);

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(roleRepository.findOneByType(RoleType.USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(userMapper.toDto(user)).thenReturn(response);

        UserResponse result = userService.createUser(dto);

        assertEquals(response, result);
        assertEquals("hashed", user.getPasswordHash());
        assertEquals(role, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_shouldThrow_whenEmailExists() {

        CreateUserDto dto = new CreateUserDto("John", "Doe", null, "mail@test.com", "pass");

        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.createUser(dto));
    }

    @Test
    void updateCurrentUser_shouldUpdateFields() {

        User user = new User();
        user.setEmail("old@mail.com");

        UpdateUserDto dto = new UpdateUserDto("New", "Name", null, "new@mail.com");

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(userMapper.toDto(user)).thenReturn(mock(UserResponse.class));

        userService.updateCurrentUser(dto);

        assertEquals("new@mail.com", user.getEmail());
        assertEquals("New", user.getName());
        assertEquals("Name", user.getSurname());
    }

    @Test
    void updateCurrentUser_shouldThrow_whenEmailTaken() {

        User user = new User();
        user.setEmail("old@mail.com");

        UpdateUserDto dto = new UpdateUserDto(null, null, null, "new@mail.com");

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.updateCurrentUser(dto));
    }

    @Test
    void changePassword_shouldEncodePassword() {

        User user = new User();

        ChangePasswordRequest request = new ChangePasswordRequest("newPass");

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.encode("newPass")).thenReturn("hashed");
        when(userMapper.toDto(user)).thenReturn(mock(UserResponse.class));

        userService.changePassword(request);

        assertEquals("hashed", user.getPasswordHash());
    }

    @Test
    void deleteUserById_shouldDelete_whenAdmin() {

        User user = new User();

        when(securityUtils.isAdmin()).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserById_shouldThrow_whenNotAdmin() {

        when(securityUtils.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> userService.deleteUserById(1L));
    }

    @Test
    void deleteUserById_shouldThrow_whenNotFound() {

        when(securityUtils.isAdmin()).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUserById(1L));
    }

}
