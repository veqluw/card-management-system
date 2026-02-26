package test.task.bankcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import test.task.bankcards.dto.request.TransferRequest;
import test.task.bankcards.entity.Card;
import test.task.bankcards.entity.Transaction;
import test.task.bankcards.entity.User;
import test.task.bankcards.exception.CardAccessDeniedException;
import test.task.bankcards.reposiory.CardRepository;
import test.task.bankcards.reposiory.TransactionRepository;
import test.task.bankcards.service.impl.TransactionServiceImpl;
import test.task.bankcards.util.SecurityUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private SecurityUtils securityUtils;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(100L);
        user.setEmail("user@gmail.com");
    }

    @Test
    void transfer_shouldSucceed_whenEnoughBalance() {

        Card from = new Card();
        from.setId(1L);
        from.setBalance(new BigDecimal("100.00"));
        from.setUser(user); // assign user!

        Card to = new Card();
        to.setId(2L);
        to.setBalance(new BigDecimal("50.00"));
        to.setUser(new User()); // recipient user

        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("30.00"));

        when(cardRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(to));
        when(securityUtils.getCurrentUser()).thenReturn(user);

        transactionService.transfer(request);

        assertEquals(new BigDecimal("70.00"), from.getBalance());
        assertEquals(new BigDecimal("80.00"), to.getBalance());

        verify(transactionRepository, Mockito.atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldFail_whenCardBelongsToAnotherUser() {

        Card from = new Card();
        from.setId(1L);
        from.setBalance(new BigDecimal("100.00"));
        User otherUser = new User();
        otherUser.setId(200L);
        from.setUser(otherUser);

        Card to = new Card();
        to.setId(2L);
        to.setBalance(new BigDecimal("50.00"));
        to.setUser(user);

        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("30.00"));

        when(cardRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(from));
        lenient().when(cardRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(to));
        when(securityUtils.getCurrentUser()).thenReturn(user);

        assertThrows(CardAccessDeniedException.class,
                () -> transactionService.transfer(request));
    }

}
