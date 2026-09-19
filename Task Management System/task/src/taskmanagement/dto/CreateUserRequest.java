package taskmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "no email given")
        @Email(message = "invalid email format")
        String email,

        @NotBlank(message = "password can't be empty")
        @Size(min = 6, message = "Password must have at least 6 characters")
        String password) {
}
