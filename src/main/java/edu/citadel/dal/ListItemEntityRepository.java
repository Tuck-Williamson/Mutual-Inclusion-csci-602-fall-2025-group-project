package edu.citadel.dal;

import edu.citadel.dal.model.ListItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListItemEntityRepository extends JpaRepository<ListItemEntity, Long> {
}
