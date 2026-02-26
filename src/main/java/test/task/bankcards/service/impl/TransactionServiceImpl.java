package test.task.bankcards.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import test.task.bankcards.dto.request.TransferRequest;
import test.task.bankcards.entity.Card;
import test.task.bankcards.entity.Transaction;
import test.task.bankcards.exception.CardAccessDeniedException;
import test.task.bankcards.exception.CardNotFoundException;
import test.task.bankcards.reposiory.CardRepository;
import test.task.bankcards.reposiory.TransactionRepository;
import test.task.bankcards.service.TransactionService;
import test.task.bankcards.util.SecurityUtils;
import test.task.bankcards.util.enums.TransactionStatus;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    @Autowired
    public TransactionServiceImpl(CardRepository cardRepository, TransactionRepository transactionRepository, SecurityUtils securityUtils) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    @Override
    public void transfer(TransferRequest request) {

        Long userId = securityUtils.getCurrentUser().getId();

        Card from = cardRepository.findByIdForUpdate(request.fromCardId())
                .orElseThrow(() -> new CardNotFoundException(request.fromCardId()));

        if (!from.getUser().getId().equals(userId)) {
            throw new CardAccessDeniedException();
        }

        Card to = cardRepository.findByIdForUpdate(request.toCardId())
                .orElseThrow(() -> new CardNotFoundException(request.toCardId()));

        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        Transaction tx = new Transaction();
        tx.setFromCard(from);
        tx.setToCard(to);
        tx.setAmount(request.amount());
        tx.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(tx);

        try {
            from.setBalance(from.getBalance().subtract(request.amount()));
            to.setBalance(to.getBalance().add(request.amount()));
            cardRepository.save(from);
            cardRepository.save(to);

            tx.setStatus(TransactionStatus.SUCCESS);
        } catch (Exception e) {
            tx.setStatus(TransactionStatus.FAILED);
            throw e;
        } finally {
            transactionRepository.save(tx);
        }
    }

}
