package test.task.bankcards.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long cardId) {
        super("Card with id " + cardId + " not found");
    }
}
