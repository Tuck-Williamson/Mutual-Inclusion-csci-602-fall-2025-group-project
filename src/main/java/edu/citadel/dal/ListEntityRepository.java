package edu.citadel.dal;

import edu.citadel.dal.model.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListEntityRepository extends JpaRepository<ListEntity, Long> {
    List<ListEntity> findByOwnerUsername(String ownerUsername);
}
