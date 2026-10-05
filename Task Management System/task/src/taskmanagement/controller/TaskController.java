package taskmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import taskmanagement.dto.*;
import taskmanagement.tasks.TaskService;

import java.util.List;


@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping
    ResponseEntity<List<TaskCreateResponseDto>> getTasks(@RequestParam(name = "author", required = false) String author,
    @RequestParam(name = "assignee", required = false) String assignee){
        //only author parameter available
        if(author != null && assignee == null){
            return ResponseEntity.ok(taskService.getUserTasks(author));
        }
        //only assignee parameter available
        if(author == null && assignee != null){
            return ResponseEntity.ok(taskService.getAssigneeTasks(assignee));
        }

        if(author != null && assignee != null){
            return ResponseEntity.ok(taskService.getByAssigneeAndByAuthor(assignee, author));
        }

        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping
    ResponseEntity<TaskCreateResponseDto> createTask(@Valid @RequestBody TaskCreateDto createDto,
                                                     Authentication authentication){
        TaskCreateResponseDto responseDto = this.taskService.createNewTask(createDto, authentication.getName());
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("{taskId}/assign")
    ResponseEntity<TaskCreateResponseDto> updateAssignee(@Valid @RequestBody AssignDto assignee, @PathVariable String taskId,
                                                         Authentication authentication){

        TaskCreateResponseDto responseDto = this.taskService.updateAssignee(authentication.getName(), taskId, assignee);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("{taskId}/status")
    ResponseEntity<TaskCreateResponseDto> updateStatus(@Valid @RequestBody StatusDto status, @PathVariable String taskId,
                                                       Authentication authentication){

        TaskCreateResponseDto response = taskService.updateTaskStatus(authentication.getName(),taskId, status );
        return ResponseEntity.ok(response);
    }

    @PostMapping("{taskId}/comments")
    ResponseEntity<Void> postComment(@Valid @RequestBody CommentDto commentDto, @PathVariable String taskId ,Authentication authentication){

    }

    @GetMapping("{taskId}/comments")
    Re



}
