package taskmanagement.dto;

import taskmanagement.comment.CommentEntity;

public record CommentResponseDto(String id, String task_id, String text, String author) {
    public static CommentResponseDto from(CommentEntity comment){

        return new CommentResponseDto(
                comment.getId().toString(),
                comment.getTask().getId(),
                comment.getText(),
                comment.getAuthor()
        );
    }
}
