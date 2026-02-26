package test.task.bankcards.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
  public RefreshTokenNotFoundException() {
    super("Refresh token not found");
  }
}

