package taskmanagement.tasks;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private String author;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TaskEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
