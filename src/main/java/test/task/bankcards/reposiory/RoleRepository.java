package test.task.bankcards.reposiory;

import org.springframework.data.jpa.repository.JpaRepository;
import test.task.bankcards.entity.Role;
import test.task.bankcards.util.enums.RoleType;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findOneByType(RoleType type);
}
