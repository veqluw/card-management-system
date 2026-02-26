package test.task.bankcards.dto.request;

import jakarta.validation.constraints.*;


import java.time.LocalDate;

public record CreateUserDto(

    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Surname is required")
    String surname,

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date should be in the past")
    LocalDate birthDate,

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8)
    String password
) {}
