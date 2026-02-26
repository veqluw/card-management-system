package test.task.bankcards.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import test.task.bankcards.dto.request.CardFilter;
import test.task.bankcards.dto.response.CardResponse;
import test.task.bankcards.entity.Card;
import test.task.bankcards.entity.User;
import test.task.bankcards.exception.CardAccessDeniedException;
import test.task.bankcards.exception.CardNotFoundException;
import test.task.bankcards.exception.InvalidCardStatusTransitionException;
import test.task.bankcards.reposiory.CardRepository;
import test.task.bankcards.service.CardService;
import test.task.bankcards.util.AesEncryptor;
import test.task.bankcards.util.enums.CardStatus;
import test.task.bankcards.util.SecurityUtils;
import test.task.bankcards.util.mapper.CardMapper;
import test.task.bankcards.util.specification.CardSpecification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final AesEncryptor aesEncryptor;
    private final CardMapper cardMapper;
    private final SecurityUtils securityUtils;

    private final Map<CardStatus, Set<CardStatus>> allowedTransitions = new HashMap<>();

    @Autowired
    public CardServiceImpl(CardRepository cardRepository,
                           CardMapper cardMapper,
                           AesEncryptor aesEncryptor,
                           SecurityUtils securityUtils){
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
        this.aesEncryptor = aesEncryptor;
        this.securityUtils = securityUtils;

        allowedTransitions.put(CardStatus.PENDING, Set.of(CardStatus.ACTIVE, CardStatus.DECLINED, CardStatus.BLOCK_REQUEST));
        allowedTransitions.put(CardStatus.ACTIVE, Set.of(CardStatus.BLOCK_REQUEST, CardStatus.BLOCKED));
        allowedTransitions.put(CardStatus.BLOCK_REQUEST, Set.of(CardStatus.BLOCKED));
        allowedTransitions.put(CardStatus.BLOCKED, Set.of());
        allowedTransitions.put(CardStatus.DECLINED, Set.of());
    }

    @Override
    public Page<CardResponse> getAll(CardFilter filter, Pageable pageable){

        Instant from = filter.fromEpochMillis() != null ? Instant.ofEpochMilli(filter.fromEpochMillis()) : null;
        Instant to = filter.toEpochMillis() != null ? Instant.ofEpochMilli(filter.toEpochMillis()) : null;

        Specification<Card> spec = Specification.where(CardSpecification.createdBetween(from, to))
                .and(CardSpecification.hasHolder(filter.holders()))
                .and(CardSpecification.hasStatuses(filter.statuses()))
                .and(CardSpecification.hasUserId(filter.userIds()));

        if (!securityUtils.isAdmin()) {
            Long currentUserId = securityUtils.getCurrentUser().getId();
            spec = spec.and(CardSpecification.hasUserId(List.of(currentUserId)));
        }

        return cardRepository.findAll(spec, pageable)
                .map(cardMapper::toDto);
    }

    @Override
    public CardResponse getOneById(Long id) {
        Card card = getCardOrThrow(id);
        validateOwnership(card);
        return cardMapper.toDto(card);
    }

    @Transactional
    @Override
    public CardResponse createCard() throws Exception{
        User user = securityUtils.getCurrentUser();
        Card card = new Card();
        card.setUser(user);
        card.setStatus(CardStatus.PENDING);
        card.setHolder(user.getName().toUpperCase() + " " + user.getSurname().toUpperCase());
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setEncryptedNumber("");
        card.setLast4("");
        card = cardRepository.save(card);

        String number = generateCardNumber(card);
        card.setLast4(number.substring(number.length() - 4));
        card.setEncryptedNumber(aesEncryptor.encrypt(number));

        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public CardResponse activateCard(Long id) {
        return changeStatus(id, CardStatus.ACTIVE);
    }

    @Override
    @Transactional
    public CardResponse requestBlockCard(Long id) {
        return changeStatus(id, CardStatus.BLOCK_REQUEST);
    }

    @Override
    @Transactional
    public CardResponse approveBlockCard(Long id) {
        return changeStatus(id, CardStatus.BLOCKED);
    }

    @Override
    @Transactional
    public CardResponse declineCard(Long id) {
        return changeStatus(id, CardStatus.DECLINED);
    }

    @Transactional
    @Override
    public void deleteCard(Long id) {
        Card card = getCardOrThrow(id);
        cardRepository.delete(card);
    }

    @Override
    public BigDecimal getTotalBalance() {
        Long userId = securityUtils.getCurrentUser().getId();
        BigDecimal balance = cardRepository.sumBalanceByUserId(userId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getBalanceByCardId(Long id) {
        Card card = getCardOrThrow(id);
        validateOwnership(card);
        return card.getBalance();
    }

    private Card getCardOrThrow(Long id) {
        return cardRepository.findOneById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
    }

    private void validateOwnership(Card card) {
        if (!securityUtils.isAdmin() && !card.getUser().getId().equals(securityUtils.getCurrentUser().getId())) {
            throw new CardAccessDeniedException();
        }
    }

    private CardResponse changeStatus(Long id, CardStatus target) {
        Card card = cardRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        validateOwnership(card);
        validateTransition(card.getStatus(), target);

        card.setStatus(target);
        return cardMapper.toDto(card);
    }

    private void validateTransition(CardStatus current, CardStatus target) {
        if (!allowedTransitions.getOrDefault(current, Set.of()).contains(target)) {
            throw new InvalidCardStatusTransitionException(current.name(), target.name());
        }
    }

    private String generateCardNumber(Card card) {
        String BIN = "400000";
        String accountPart = String.format("%09d", card.getId());
        String partial = BIN + accountPart;

        int checkDigit = createCheckDigit(partial);
        return partial + checkDigit;
    }

    private int createCheckDigit(String partial) {
        boolean shouldDouble = false;
        int sum = 0;
        for (int i = partial.length() - 1; i    >= 0; i--) {
            int digit = partial.charAt(i) - '0';
            if (shouldDouble) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            shouldDouble = !shouldDouble;
        }
        return (10 - (sum % 10)) % 10;
    }
}