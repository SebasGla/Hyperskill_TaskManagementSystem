package taskmanagement.tasks;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface TaskRepository extends CrudRepository<TaskEntity,String> {
    List<TaskEntity> findByOrderByCreatedAtDesc();
    List<TaskEntity> findAllByAuthorOrderByCreatedAtDesc(String author);
    List<TaskEntity> findAllByAssigneeOrderByCreatedAtDesc(String assignee);
    List<TaskEntity> findAllByAssigneeAndAuthorOrderByCreatedAtDesc(String assignee, String author);
    TaskEntity getById(String id);

}
