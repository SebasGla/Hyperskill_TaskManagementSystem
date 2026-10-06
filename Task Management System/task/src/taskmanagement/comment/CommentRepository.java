package taskmanagement.comment;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<CommentEntity, Long> {

    @Query("SELECT c FROM CommentEntity c WHERE c.task.id = :taskId ORDER BY c.createdAt ASC")
    List<CommentEntity> findAllByTaskId(@Param("taskId") String taskId);
}

