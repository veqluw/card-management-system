package test.task.bankcards.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import test.task.bankcards.dto.request.CardFilter;
import test.task.bankcards.dto.response.CardResponse;
import test.task.bankcards.service.CardService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping()
    public ResponseEntity<CardResponse> createCard() throws Exception {
        return ResponseEntity.ok(cardService.createCard());
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAll(
           @ModelAttribute CardFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                cardService.getAll(filter, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getOneById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(cardService.getOneById(id));
    }

    @GetMapping("/balance/get/total")
    public ResponseEntity<BigDecimal> getTotalBalance() {
        return ResponseEntity.ok(cardService.getTotalBalance());
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalanceByCardId(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(cardService.getBalanceByCardId(id));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> activateCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.activateCard(id));
    }

    @PutMapping("/{id}/block/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> approveBlockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.approveBlockCard(id));
    }

    @PutMapping("/{id}/block/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardResponse> requestBlockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.requestBlockCard(id));
    }

    @PutMapping("/{id}/decline")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> declineCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.declineCard(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

}
