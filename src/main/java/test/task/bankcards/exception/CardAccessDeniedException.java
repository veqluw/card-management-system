package test.task.bankcards.exception;

public class CardAccessDeniedException extends RuntimeException {
    public CardAccessDeniedException() {
        super("You do not have access to this card");
    }
}
