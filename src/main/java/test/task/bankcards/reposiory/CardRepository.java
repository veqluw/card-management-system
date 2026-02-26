package test.task.bankcards.reposiory;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import test.task.bankcards.entity.Card;
import test.task.bankcards.util.enums.CardStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    Optional<Card> findOneById(Long id);
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);
    List<Card> findAllByStatus(CardStatus cardStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :id")
    Optional<Card> findByIdForUpdate(Long id);

    @Query("select sum(c.balance) from Card c where c.user.id = :userId")
    BigDecimal sumBalanceByUserId(Long userId);

}
