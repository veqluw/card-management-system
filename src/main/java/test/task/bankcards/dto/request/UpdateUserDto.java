package test.task.bankcards.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateUserDto(

        String name,

        String surname,

        @Past(message = "Birth date should be in the past")
        LocalDate birthDate,

        @Email(message = "Email should be valid")
        String email
) {
}
