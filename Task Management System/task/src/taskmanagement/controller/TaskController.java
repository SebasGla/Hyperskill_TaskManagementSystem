package taskmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import taskmanagement.dto.AssignDto;
import taskmanagement.dto.StatusDto;
import taskmanagement.dto.TaskCreateDto;
import taskmanagement.dto.TaskCreateResponseDto;
import taskmanagement.tasks.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping
    ResponseEntity<List<TaskCreateResponseDto>> getTasks(@RequestParam(name = "author", required = false) String author){
        if(author != null){
            return ResponseEntity.ok(taskService.getUserTasks(author.toLowerCase()));
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
    ResponseEntity<TaskCreateResponseDto> updateAssignee(@Valid @RequestBody AssignDto assignee, @PathVariable UUID taskId,
                                                         Authentication authentication){
        if (!this.taskService.checkTaskExists(taskId)){
            return ResponseEntity.notFound().build();
        }

        if(!this.taskService.checkAssigneeFits(authentication.getName(), taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TaskCreateResponseDto responseDto = this.taskService.updateAssignee(assignee, taskId);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("{taskId}/status")
    ResponseEntity<TaskCreateResponseDto> updateStatus(@Valid StatusDto status, @PathVariable UUID taskId,
                                                       Authentication authentication){

        TaskCreateResponseDto response = taskService.updateTaskStatus(authentication.getName(),taskId, status );

        return ResponseEntity.ok(response);


    }

}
