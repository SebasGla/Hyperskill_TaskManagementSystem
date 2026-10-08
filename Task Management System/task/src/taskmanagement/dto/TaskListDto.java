package taskmanagement.dto;

import taskmanagement.tasks.TaskEntity;
import taskmanagement.tasks.TaskStatus;

public record TaskListDto(String id, String title, String description,
                                    TaskStatus status, String author, String assignee, int total_comments) {
    public static TaskListDto from(TaskEntity entity){
        return new TaskListDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getAuthor(),
                entity.getAssignee(),
                entity.getTotalComments()
        );
    }
}