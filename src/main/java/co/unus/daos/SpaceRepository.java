package co.unus.daos;

import co.unus.models.Space;
import co.unus.models.UnusUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {
    Optional<Space> findByCode(String code);

    @Query("SELECT CASE WHEN count(s) > 0 THEN true ELSE false END " +
            "FROM Space s " +
            "WHERE s.code = :code AND EXISTS (" +
                "SELECT 1 " +
                "FROM s.members m " +
                "WHERE m.email = :userEmail" +
            ")")
    Boolean isMember(@Param("code") String code, @Param("userEmail") String userEmail);
}
