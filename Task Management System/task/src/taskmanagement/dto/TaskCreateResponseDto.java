package taskmanagement.dto;

import taskmanagement.comment.CommentEntity;
import taskmanagement.tasks.TaskEntity;
import taskmanagement.tasks.TaskStatus;

import java.util.Collections;
import java.util.List;

public record TaskCreateResponseDto(String id, String title, String description,
                                    TaskStatus status, String author, String assignee, int total_comments) {
    public static TaskCreateResponseDto from(TaskEntity entity){
        return new TaskCreateResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getAuthor(),
                entity.getAssignee(),

        );
    }
}
