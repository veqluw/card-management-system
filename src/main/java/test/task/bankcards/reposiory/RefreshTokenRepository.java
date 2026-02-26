package test.task.bankcards.reposiory;

import org.springframework.data.jpa.repository.JpaRepository;
import test.task.bankcards.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(String jti);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
