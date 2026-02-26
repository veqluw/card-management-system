package test.task.bankcards.exception;

public class MissingTokenException extends RuntimeException {

    public MissingTokenException(String message) {
        super(message);
    }

}