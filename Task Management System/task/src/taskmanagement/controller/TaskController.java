package taskmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import taskmanagement.dto.TaskCreateDto;
import taskmanagement.dto.TaskCreateResponseDto;
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
    ResponseEntity<List<TaskCreateResponseDto>> getTask(){
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping
    ResponseEntity<TaskCreateResponseDto> createTask(@Valid @RequestBody TaskCreateDto createDto,
                                                     @AuthenticationPrincipal UserDetails userDetails){
        TaskCreateResponseDto responseDto = this.taskService.createNewTask(createDto, userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }
}
