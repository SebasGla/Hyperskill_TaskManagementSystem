package taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskCreateDto(@NotBlank String title, @NotBlank String description) {
}
