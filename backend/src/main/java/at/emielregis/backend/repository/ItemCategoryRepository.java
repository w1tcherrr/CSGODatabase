package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link ItemCategory} entities.
 * Provides methods to retrieve item categories by their attributes.
 */
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {

    /**
     * Finds an item category by its name.
     *
     * @param name The name of the category.
     * @return The {@link ItemCategory} entity, or null if not found.
     */
    ItemCategory findByName(String name);
}
