package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemCollection;
import at.emielregis.backend.data.entities.items.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Repository interface for {@link ItemCollection} entities.
 * Provides methods for managing and querying item collections.
 */
public interface ItemRepository extends JpaRepository<ItemCollection, Long> {

    /**
     * Counts the total number of items across all collections.
     *
     * @return The total count of items.
     */
    @Query("SELECT sum(i.amount) FROM ItemCollection i")
    long countTotalItems();

    /**
     * Deletes an item collection by its ID.
     *
     * @param id The ID of the collection to delete.
     */
    @Modifying
    @Query("DELETE FROM ItemCollection i WHERE i.id = :id")
    void deleteById(@Param("id") Long id);

    /**
     * Sums the amounts for a list of item types.
     *
     * @param itemType The list of item types to filter by.
     * @return The sum of amounts for the specified item types.
     */
    @Query("SELECT sum(i.amount) FROM ItemCollection i WHERE i.itemType IN :types")
    Long sumForItemTypes(@Param("types") List<ItemType> itemType);

    /**
     * Retrieves IDs of orphaned item collections not linked to any inventory.
     *
     * @return A set of orphaned item collection IDs.
     */
    @Query("SELECT i.id FROM ItemCollection i WHERE i NOT IN (SELECT i1 FROM CSGOInventory inv JOIN inv.itemCollections i1)")
    Set<Long> getOrphanedItemIds();

    /**
     * Gets the total amount for a specific item type.
     *
     * @param type The {@link ItemType} to filter by.
     * @return The total amount for the specified item type.
     */
    @Query("SELECT sum(i.amount) FROM ItemCollection i WHERE i.itemType = :type")
    int getTotalAmountForType(@Param("type") ItemType type);

    /**
     * Retrieves the counts of items grouped by name tags.
     *
     * @return A list of object arrays where each contains the name tag and its total count.
     */
    @Query("SELECT i.nameTag, SUM(i.amount) FROM ItemCollection i WHERE i.nameTag IS NOT NULL GROUP BY i.nameTag")
    List<Object[]> getNameTagCounts();
}
