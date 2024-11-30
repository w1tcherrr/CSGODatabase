package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for {@link ItemName} entities.
 * Provides methods for managing and querying item names.
 */
public interface ItemNameRepository extends JpaRepository<ItemName, Long> {

    /**
     * Retrieves item names that match a filter string, case-insensitively.
     *
     * @param filter The filter string to search for.
     * @return A list of {@link ItemName} entities matching the filter.
     */
    @Query("SELECT i FROM ItemName i WHERE UPPER(i.name) LIKE UPPER(CONCAT('%', :filter, '%'))")
    List<ItemName> getSearch(@Param("filter") String filter);

    /**
     * Finds an item name by its exact name.
     *
     * @param name The exact name of the item.
     * @return The {@link ItemName} entity, or null if not found.
     */
    ItemName findByName(String name);

    /**
     * Checks if a given item name has associated exteriors.
     *
     * @param itemName The {@link ItemName} to check.
     * @return {@code true} if the item name has associated exteriors, {@code false} otherwise.
     */
    @Query("SELECT COUNT(i) > 0 FROM ItemType i WHERE i.itemName = :name AND i.exterior IS NOT NULL")
    boolean itemNameHasExteriors(@Param("name") ItemName itemName);

    /**
     * Retrieves all distinct item names associated with a specific item set.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return A list of {@link ItemName} entities in the given item set.
     */
    @Query("SELECT DISTINCT i.itemName FROM ItemType i WHERE i.itemSet = :set")
    List<ItemName> getAllNamesForSet(@Param("set") ItemSet set);
}
