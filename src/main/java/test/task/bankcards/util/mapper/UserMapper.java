package test.task.bankcards.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import test.task.bankcards.dto.request.CreateUserDto;
import test.task.bankcards.dto.response.UserResponse;
import test.task.bankcards.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserDto createUserDto);

    UserResponse toDto(User user);
}
