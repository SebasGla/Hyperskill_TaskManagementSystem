package taskmanagement.dto;

import jakarta.validation.constraints.NotNull;
import taskmanagement.tasks.TaskStatus;

public record StatusDto(@NotNull(message = "Task empty")
                        TaskStatus status) {
}
