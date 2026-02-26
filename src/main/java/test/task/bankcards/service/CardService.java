package test.task.bankcards.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import test.task.bankcards.dto.request.CardFilter;
import test.task.bankcards.dto.response.CardResponse;

import java.math.BigDecimal;

public interface CardService {

    Page<CardResponse> getAll(CardFilter filter, Pageable pageable);
    CardResponse getOneById(Long id);
    CardResponse createCard() throws Exception;
    CardResponse activateCard(Long id);
    CardResponse approveBlockCard(Long id);
    CardResponse requestBlockCard(Long id);
    CardResponse declineCard(Long id);
    void deleteCard(Long id);
    BigDecimal getTotalBalance();
    BigDecimal getBalanceByCardId(Long id);
}
