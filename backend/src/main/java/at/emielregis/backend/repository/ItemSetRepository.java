package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.ItemSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemSetRepository extends JpaRepository<ItemSet, Long> {
    boolean existsByName(String name);

    ItemSet getByName(String name);
}
