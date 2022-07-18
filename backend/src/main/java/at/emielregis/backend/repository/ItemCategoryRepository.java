package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    ItemCategory findByName(String name);
}
