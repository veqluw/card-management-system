package test.task.bankcards.reposiory;

import org.springframework.data.jpa.repository.JpaRepository;
import test.task.bankcards.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
