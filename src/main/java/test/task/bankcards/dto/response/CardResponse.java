package test.task.bankcards.dto.response;

import lombok.Data;
import test.task.bankcards.util.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardResponse {

    String maskedCardNumber;
    String holder;
    LocalDate expirationDate;
    CardStatus status;
    BigDecimal balance;

}
