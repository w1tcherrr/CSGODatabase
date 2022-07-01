package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.ItemName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemNameRepository extends JpaRepository<ItemName, Long> {
    boolean existsByName(String name);

    ItemName getByName(String name);
}
