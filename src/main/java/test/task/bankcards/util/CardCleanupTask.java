package test.task.bankcards.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import test.task.bankcards.entity.Card;
import test.task.bankcards.reposiory.CardRepository;
import test.task.bankcards.util.enums.CardStatus;

import java.util.List;

@Component
public class CardCleanupTask {

    private final CardRepository cardRepository;

    @Autowired
    public CardCleanupTask(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 900000)
    public void deleteDeclinedCards() {
        List<Card> declined = cardRepository.findAllByStatus(CardStatus.DECLINED);
        cardRepository.deleteAll(declined);
    }
}
