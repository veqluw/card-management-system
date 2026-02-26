package test.task.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import test.task.bankcards.util.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Card extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NonNull
    @Column(nullable = false, unique = true)
    private String encryptedNumber;

    @NonNull
    @Column(nullable = false)
    private String last4;

    @Column(nullable = false)
    private String holder;

    @Column(nullable = false, name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.PENDING;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
}
