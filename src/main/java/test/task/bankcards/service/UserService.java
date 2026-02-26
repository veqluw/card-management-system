package test.task.bankcards.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import test.task.bankcards.dto.request.ChangePasswordRequest;
import test.task.bankcards.dto.request.CreateUserDto;
import test.task.bankcards.dto.request.UpdateUserDto;
import test.task.bankcards.dto.request.UserFilter;
import test.task.bankcards.dto.response.UserResponse;

public interface UserService {

    Page<UserResponse> getAll(UserFilter filter, Pageable pageable);
    UserResponse getOneById(Long id);
    UserResponse createUser(CreateUserDto createUserDto);
    UserResponse updateCurrentUser(UpdateUserDto updateUserDto);
    UserResponse changePassword(ChangePasswordRequest request);
    UserResponse getCurrentUser();
    void deleteUserById(Long id);

}
