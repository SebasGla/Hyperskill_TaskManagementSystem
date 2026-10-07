package taskmanagement.tasks;


import org.springframework.stereotype.Service;
import taskmanagement.dto.*;
import taskmanagement.exception.AssigneeNotFoundException;
import taskmanagement.exception.TaskForbiddenException;
import taskmanagement.exception.TaskNotFoundException;
import taskmanagement.user.UserRepository;

import java.util.List;

import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;


    public TaskService(TaskRepository taskRepository, UserRepository userRepository){

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
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

    public TaskCreateResponseDto updateAssignee(String username, String uuid, AssignDto assignDto){
        TaskEntity task = this.taskRepository.findById(uuid.toLowerCase()).orElseThrow(
                () -> new TaskNotFoundException(uuid+" not Found"));

        if(!task.getAuthor().equals(username.toLowerCase())){
            throw new TaskForbiddenException("User not allowed to change assignee");
        }

        String newAssignee = assignDto.assignee().toLowerCase();

        if (!"none".equals(newAssignee)) {
            if (!this.userRepository.existsByEmail(newAssignee)) {
                throw new AssigneeNotFoundException("Assignee does not exist");
            }
        }

        task.setAssignee(newAssignee);
        this.taskRepository.save(task);
        return TaskCreateResponseDto.from(task);
    }

    public List<TaskCreateResponseDto> getAllTasks(){
        return taskRepository.findByOrderByCreatedAtDesc().stream().map(TaskCreateResponseDto::from).toList();
    }

    public List<TaskCreateResponseDto> getUserTasks(String username){
        return taskRepository.findAllByAuthorOrderByCreatedAtDesc(username.toLowerCase()).stream().map(TaskCreateResponseDto::from).toList();
    }

    public List<TaskCreateResponseDto> getAssigneeTasks(String assignee){
        return taskRepository.findAllByAssigneeOrderByCreatedAtDesc(assignee.toLowerCase()).stream().map(TaskCreateResponseDto::from).toList();
    }

    public List<TaskCreateResponseDto> getByAssigneeAndByAuthor(String assignee, String username){
        return taskRepository.findAllByAssigneeAndAuthorOrderByCreatedAtDesc(assignee.toLowerCase(), username.toLowerCase()).stream().map(TaskCreateResponseDto::from).toList();
    }

    public TaskCreateResponseDto updateTaskStatus(String username, String uuid, StatusDto status){
       TaskEntity task = taskRepository.findById(uuid.toLowerCase())
               .orElseThrow(() -> new TaskNotFoundException(uuid+" not Found"));

       if(!(task.getAssignee().equals(username) || task.getAuthor().equals(username))){
           throw new TaskForbiddenException("Only Author or Assignee can change task status!");
       }

       task.setStatus(status.status());
       this.taskRepository.save(task);

       return TaskCreateResponseDto.from(task);
    }
}
