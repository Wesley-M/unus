package co.unus.daos;

import co.unus.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long>  {
    @Query("SELECT CASE WHEN count(g) > 0 THEN true ELSE false END " +
            "FROM Group g " +
            "WHERE g.space.code = :spaceCode AND g.name = :groupName")
    Boolean alreadyExistsInSpace(@Param("spaceCode") String spaceCode, @Param("groupName") String groupName);

    @Query("SELECT CASE WHEN count(g) > 0 THEN true ELSE false END " +
            "FROM Group g " +
            "WHERE g.space.code = :code AND EXISTS (" +
                "SELECT 1 " +
                "FROM g.members m " +
                "WHERE m.email = :userEmail" +
            ") OR g.admin.email = :userEmail")
    Boolean hasGroupInSpace(@Param("code") String code, @Param("userEmail") String userEmail);
}
