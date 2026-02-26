package test.task.bankcards.exception;

public class InvalidCardStatusTransitionException extends RuntimeException {
    public InvalidCardStatusTransitionException(String from, String to) {
        super("Cannot transition card status from " + from + " to " + to);
    }
}
