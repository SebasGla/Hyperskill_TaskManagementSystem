package taskmanagement.comment;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import taskmanagement.dto.CommentDto;
import taskmanagement.dto.CommentResponseDto;
import taskmanagement.exception.TaskNotFoundException;
import taskmanagement.tasks.TaskEntity;
import taskmanagement.tasks.TaskRepository;

import java.util.List;

@Service
public class CommentService {
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;

    public CommentService(TaskRepository taskRepository, CommentRepository commentRepository){
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
    }

    public ResponseEntity<Void> addComment(String taskId, String user, CommentDto dto){
        TaskEntity task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("No such Task"));
        CommentEntity comment = new CommentEntity();
        comment.setTask(task);
        comment.setAuthor(user.toLowerCase());
        comment.setText(dto.text());
        commentRepository.save(comment);

        return ResponseEntity.ok().build();
    }

    public List<CommentResponseDto> getTaskCommentList(String taskId){
        List<CommentResponseDto> commentList;
        if(taskRepository.existsById(taskId)){
           commentList = commentRepository.findAllByTaskId(taskId).stream().map(CommentResponseDto::from).toList();
       }
       else {
           throw new TaskNotFoundException("No such Task");
       }

       return commentList;
    }

    public int amountComments(String taskId){
        return  this.commentRepository.countByTask_Id(taskId);
    }
}
