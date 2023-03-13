package co.unus.daos;

import co.unus.models.UnusUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnusUserRepository extends JpaRepository<UnusUser, Long> {
    Optional<UnusUser> findByEmail(String username);
}
