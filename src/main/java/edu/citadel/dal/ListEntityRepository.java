package edu.citadel.dal;

import edu.citadel.dal.model.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListEntityRepository extends JpaRepository<ListEntity, Long> {
    @Query(
            value = "SELECT l.* " +
                    "FROM list as l JOIN accounts as a" +
                    " ON l.user_id = a.user_id" +
                    " WHERE a.user_id in (" +
                    "select user_id from accounts where" +
                    " username = :username)",
            nativeQuery = true
    )
    List<ListEntity> findByOwnerUsername(@Param("username") String ownerUsername);
}
