package taskmanagement.tasks;

import org.springframework.stereotype.Service;
import taskmanagement.dto.TaskCreateDto;
import taskmanagement.dto.TaskCreateResponseDto;

import java.util.List;

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
        this.taskRepository.save(task);
        return TaskCreateResponseDto.from(task);
    }

    public List<TaskCreateResponseDto> getAllTasks(){
        return taskRepository.findByOrderByCreatedAtDesc().stream().map(TaskCreateResponseDto::from).toList();
    }

    public List<TaskCreateResponseDto> getUserTasks(String username){
        return taskRepository.findAllByAuthorOrderByCreatedAtDesc(username).stream().map(TaskCreateResponseDto::from).toList();
    }
}
