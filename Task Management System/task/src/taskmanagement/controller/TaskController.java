package taskmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @GetMapping
    ResponseEntity<Void> getTask(){
        return ResponseEntity.ok().build();
    }

    @PostMapping
    ResponseEntity<TaskCreatResponseDto> createTask(TaskCreateDto createDto){

    }
}
