package taskmanagement.comment;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import taskmanagement.dto.CommentDto;
import taskmanagement.exception.TaskNotFoundException;
import taskmanagement.tasks.TaskEntity;
import taskmanagement.tasks.TaskRepository;

@Service
public class CommentService {
    private TaskRepository taskRepository;
    private CommentRepository commentRepository;

    public CommentService(TaskRepository taskRepository, CommentRepository commentRepository){
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
    }

    ResponseEntity<Void> addComment(String taskId, CommentDto dto){
        TaskEntity task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("No such Task"));
        CommentEntity comment = new CommentEntity();


    }
}
