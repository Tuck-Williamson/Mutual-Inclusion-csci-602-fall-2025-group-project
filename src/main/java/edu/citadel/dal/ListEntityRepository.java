package edu.citadel.dal;

import edu.citadel.dal.model.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListEntityRepository extends JpaRepository<ListEntity, Long> {
}
