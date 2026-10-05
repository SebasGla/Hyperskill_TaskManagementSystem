package taskmanagement.dto;

import jakarta.validation.constraints.NotEmpty;

public record CommentDto(@NotEmpty String text) {
}
