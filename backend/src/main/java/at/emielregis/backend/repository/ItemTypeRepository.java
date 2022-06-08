package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemTypeRepository extends JpaRepository<ItemType, Long> {
    boolean existsByName(String name);

    ItemType getByName(String name);
}
