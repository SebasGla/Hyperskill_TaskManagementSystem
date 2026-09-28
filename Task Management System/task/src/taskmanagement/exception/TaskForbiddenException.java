package taskmanagement.exception;

public class TaskForbiddenException extends RuntimeException{
    public TaskForbiddenException(String message) {
        super(message);
    }
}
