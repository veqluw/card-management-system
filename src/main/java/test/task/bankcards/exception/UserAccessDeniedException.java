package test.task.bankcards.exception;

public class UserAccessDeniedException extends RuntimeException {
    public UserAccessDeniedException() {
        super("Admin privileges required");
    }
}
