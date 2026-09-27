package taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AssignDto(@NotBlank(message = "Assignee empty")
                        @Pattern(
                                regexp = "^(none|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$",
                                message = "Assignee must be 'none' or a valid Email"
                        )String assignee) {
}
