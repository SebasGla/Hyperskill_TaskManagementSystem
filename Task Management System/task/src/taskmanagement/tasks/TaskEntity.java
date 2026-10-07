package taskmanagement.tasks;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import taskmanagement.comment.CommentEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    private String assignee;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();

    public TaskEntity() {
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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

    public List<CommentEntity> getComments() {
        return comments;
    }

    public void setComments(List<CommentEntity> comments) {
        this.comments = comments;
    }

    // Best Practice: Helper-Methoden halten beide Seiten synchron
    public void addComment(CommentEntity comment) {
        this.comments.add(comment);
        comment.setTask(this);
    }

    public void removeComment(CommentEntity comment) {
        this.comments.remove(comment);
        comment.setTask(null);
    }
}
