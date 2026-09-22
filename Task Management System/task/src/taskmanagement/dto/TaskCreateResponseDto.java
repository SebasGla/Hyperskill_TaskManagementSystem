package taskmanagement.dto;

import taskmanagement.tasks.TaskEntity;
import taskmanagement.tasks.TaskStatus;

public record TaskCreateResponseDto(String id, String title, String description, TaskStatus status, String author) {
    public static TaskCreateResponseDto from(TaskEntity entity){
        return new TaskCreateResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getAuthor()
        );
    }
}
