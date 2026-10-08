package taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;


public record CommentDto(@NotBlank String text) {
}
