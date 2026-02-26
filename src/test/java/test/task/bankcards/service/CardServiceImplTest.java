package test.task.bankcards.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import test.task.bankcards.dto.response.CardResponse;
import test.task.bankcards.entity.Card;
import test.task.bankcards.entity.User;
import test.task.bankcards.exception.CardAccessDeniedException;
import test.task.bankcards.exception.InvalidCardStatusTransitionException;
import test.task.bankcards.reposiory.CardRepository;
import test.task.bankcards.service.impl.CardServiceImpl;
import test.task.bankcards.util.AesEncryptor;
import test.task.bankcards.util.enums.CardStatus;
import test.task.bankcards.util.SecurityUtils;
import test.task.bankcards.util.mapper.CardMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock private CardMapper cardMapper;
    @Mock private AesEncryptor aesEncryptor;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void getOneById_shouldReturnDto_ifOwner() {
        User user = new User();
        user.setId(1L);

        Card card = new Card();
        card.setId(10L);
        card.setUser(user);

        CardResponse dto = mock(CardResponse.class);

        when(cardRepository.findOneById(10L)).thenReturn(Optional.of(card));
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(cardMapper.toDto(card)).thenReturn(dto);

        CardResponse result = cardService.getOneById(10L);

        assertEquals(dto, result);
    }

    @Test
    void getOneById_shouldThrow_ifNotOwner() {
        User owner = new User();
        owner.setId(1L);

        User current = new User();
        current.setId(2L);

        Card card = new Card();
        card.setId(10L);
        card.setUser(owner);

        when(cardRepository.findOneById(10L)).thenReturn(Optional.of(card));
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(current);

        assertThrows(CardAccessDeniedException.class,
                () -> cardService.getOneById(10L));
    }

    @Test
    void createCard_shouldSetFields_andEncryptNumber() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setSurname("Doe");

        Card card = new Card();
        card.setId(100L);

        CardResponse dto = mock(CardResponse.class);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });
        when(aesEncryptor.encrypt(anyString())).thenReturn("encrypted-number");
        when(cardMapper.toDto(any(Card.class))).thenReturn(dto);

        CardResponse result = cardService.createCard();

        assertEquals(dto, result);
    }

    @Test
    void activateCard_shouldChangeStatus() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.PENDING);
        User user = new User();
        user.setId(1L);
        card.setUser(user);

        CardResponse dto = mock(CardResponse.class);

        when(cardRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(card));
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(cardMapper.toDto(card)).thenReturn(dto);

        CardResponse result = cardService.activateCard(1L);

        assertEquals(dto, result);
        assertEquals(CardStatus.ACTIVE, card.getStatus());
    }

    @Test
    void changeStatus_shouldThrow_ifInvalidTransition() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.DECLINED);
        User user = new User();
        user.setId(1L);
        card.setUser(user);

        when(cardRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(card));
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(user);

        assertThrows(InvalidCardStatusTransitionException.class, () -> cardService.activateCard(1L));
    }

    @Test
    void getTotalBalance_shouldReturnSum_orZero() {
        User user = new User();
        user.setId(1L);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(cardRepository.sumBalanceByUserId(1L)).thenReturn(new BigDecimal("100.50"));

        assertEquals(new BigDecimal("100.50"), cardService.getTotalBalance());

        when(cardRepository.sumBalanceByUserId(1L)).thenReturn(null);

        assertEquals(BigDecimal.ZERO, cardService.getTotalBalance());
    }

    @Test
    void getBalanceByCardId_shouldReturnBalance_ifOwner() {
        Card card = new Card();
        card.setBalance(new BigDecimal("50.00"));
        User user = new User();
        user.setId(1L);
        card.setUser(user);

        when(cardRepository.findOneById(1L)).thenReturn(Optional.of(card));
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(user);

        assertEquals(new BigDecimal("50.00"), cardService.getBalanceByCardId(1L));
    }

    @Test
    void deleteCard_shouldCallRepositoryDelete() {
        Card card = new Card();
        card.setId(1L);
        User user = new User();
        user.setId(1L);
        card.setUser(user);

        when(cardRepository.findOneById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        verify(cardRepository).delete(card);
    }

}
