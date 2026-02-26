package test.task.bankcards.exception;

public class TokenHashException extends RuntimeException {
    public TokenHashException(Throwable cause) {
        super("Error hashing token", cause);
    }
}
