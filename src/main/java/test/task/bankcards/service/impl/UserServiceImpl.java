package test.task.bankcards.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import test.task.bankcards.dto.request.ChangePasswordRequest;
import test.task.bankcards.dto.request.CreateUserDto;
import test.task.bankcards.dto.request.UpdateUserDto;
import test.task.bankcards.dto.request.UserFilter;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.entity.Role;
import test.task.bankcards.entity.User;
import test.task.bankcards.exception.EmailAlreadyUsedException;
import test.task.bankcards.exception.RoleNotFoundException;
import test.task.bankcards.exception.UserNotFoundException;
import test.task.bankcards.reposiory.RoleRepository;
import test.task.bankcards.reposiory.UserRepository;
import test.task.bankcards.service.UserService;
import test.task.bankcards.util.enums.RoleType;
import test.task.bankcards.util.SecurityUtils;
import test.task.bankcards.util.mapper.UserMapper;
import test.task.bankcards.util.specification.UserSpecification;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final SecurityUtils securityUtils;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            BCryptPasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            SecurityUtils securityUtils
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    public Page<UserResponse> getAll(UserFilter filter, Pageable pageable) {

        Instant from = filter.fromEpochMillis() != null ? Instant.ofEpochMilli(filter.fromEpochMillis()) : null;
        Instant to = filter.toEpochMillis() != null ? Instant.ofEpochMilli(filter.toEpochMillis()) : null;

        Specification<User> spec = Specification.where(UserSpecification.createdBetween(from, to))
                .and(UserSpecification.hasName(filter.names()))
                .and(UserSpecification.hasSurname(filter.surnames()));

        return userRepository
                .findAll(spec, pageable)
                .map(userMapper::toDto);
    }

    @Override
    public UserResponse getCurrentUser() {
        return userMapper.toDto(securityUtils.getCurrentUser());
    }

    @Override
    public UserResponse getOneById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email())) {
            throw new EmailAlreadyUsedException(createUserDto.email());
        }

        User user = userMapper.toEntity(createUserDto);
        Role defaultRole = roleRepository.findOneByType(RoleType.USER)
                .orElseThrow(() -> new RoleNotFoundException(RoleType.USER));

        user.setRole(defaultRole);

        String passwordHash = passwordEncoder.encode(createUserDto.password());
        user.setPasswordHash(passwordHash);

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public UserResponse updateCurrentUser(UpdateUserDto dto) {

        User user = securityUtils.getCurrentUser();

        if (dto.email() != null && !dto.email().equals(user.getEmail())) {

            if (userRepository.existsByEmail(dto.email())) {
                throw new EmailAlreadyUsedException(dto.email());
            }

            user.setEmail(dto.email());
        }

        if (dto.name() != null) user.setName(dto.name());
        if (dto.surname() != null) user.setSurname(dto.surname());
        if (dto.birthDate() != null) user.setBirthDate(dto.birthDate());

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponse changePassword(ChangePasswordRequest request) {
        User user = securityUtils.getCurrentUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public void deleteUserById(Long id) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Admin privileges required");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);
    }
}
