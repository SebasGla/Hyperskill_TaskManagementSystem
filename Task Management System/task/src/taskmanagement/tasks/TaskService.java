package taskmanagement.tasks;


import org.springframework.stereotype.Service;
import taskmanagement.dto.AssignDto;
import taskmanagement.dto.StatusDto;
import taskmanagement.dto.TaskCreateDto;
import taskmanagement.dto.TaskCreateResponseDto;
import taskmanagement.exception.TaskForbiddenException;
import taskmanagement.exception.TaskNotFoundException;

import java.util.List;

import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){

        this.taskRepository = taskRepository;
    }

    public TaskCreateResponseDto createNewTask(TaskCreateDto taskDto, String username){
        TaskEntity task = new TaskEntity();
        task.setTitle(taskDto.title());
        task.setDescription(taskDto.description());
        task.setStatus(TaskStatus.CREATED);
        task.setAuthor(username.toLowerCase());
        task.setAssignee("none");
        this.taskRepository.save(task);
        return TaskCreateResponseDto.from(task);
    }

    public TaskCreateResponseDto updateAssignee(AssignDto assignDto, UUID uuid){
        TaskEntity task = this.taskRepository.getById(uuid.toString());
        task.setAssignee(assignDto.assignee().toLowerCase());
        this.taskRepository.save(task);
        return TaskCreateResponseDto.from(task);
    }

    public List<TaskCreateResponseDto> getAllTasks(){
        return taskRepository.findByOrderByCreatedAtDesc().stream().map(TaskCreateResponseDto::from).toList();
    }

    public List<TaskCreateResponseDto> getUserTasks(String username){
        return taskRepository.findAllByAuthorOrderByCreatedAtDesc(username).stream().map(TaskCreateResponseDto::from).toList();
    }

    public boolean checkTaskExists(UUID uuid){
        return taskRepository.existsById(uuid.toString().toLowerCase());
    }

    public boolean checkAssigneeFits(String username, UUID uuid){
        TaskEntity task = this.taskRepository.getById(uuid.toString().toLowerCase());
        return task.getAuthor().equals(username.toLowerCase());
    }

    public TaskCreateResponseDto updateTaskStatus(String username, UUID uuid, StatusDto status){
       TaskEntity task = taskRepository.findById(uuid.toString().toLowerCase())
               .orElseThrow(() -> new TaskNotFoundException(uuid+" not Found"));

       if(!(task.getAssignee().equals(username) || task.getAuthor().equals(username))){
           throw new TaskForbiddenException("Only Author or Assignee can change task status!");
       }

       task.setStatus(status.status());
       this.taskRepository.save(task);

       return TaskCreateResponseDto.from(task);
    }

}
