package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for {@link ItemSet} entities.
 * Provides methods for managing and querying item sets.
 */
public interface ItemSetRepository extends JpaRepository<ItemSet, Long> {

    /**
     * Finds an item set by its exact name.
     *
     * @param name The name of the item set.
     * @return The {@link ItemSet} entity, or null if not found.
     */
    ItemSet getByName(String name);

    /**
     * Retrieves all distinct exteriors for a specific item set.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return A list of distinct {@link Exterior} values for the given set.
     */
    @Query("SELECT DISTINCT i.exterior FROM ItemType i WHERE i.itemSet = :set AND i.exterior IS NOT NULL")
    List<Exterior> getExteriorsForSet(@Param("set") ItemSet set);

    /**
     * Checks if an item set contains any items with the StatTrak attribute.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return {@code true} if the set has StatTrak items, {@code false} otherwise.
     */
    @Query("SELECT count(i) > 0 FROM ItemType i WHERE i.itemSet = :set AND i.specialItemType = at.emielregis.backend.data.enums.SpecialItemType.STAT_TRAK")
    boolean hasStatTrakForItemSet(@Param("set") ItemSet set);

    /**
     * Checks if an item set contains any items with the Souvenir attribute.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return {@code true} if the set has Souvenir items, {@code false} otherwise.
     */
    @Query("SELECT count(i) > 0 FROM ItemType i WHERE i.itemSet = :set AND i.specialItemType = at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR")
    boolean hasSouvenirForItemSet(@Param("set") ItemSet set);

    /**
     * Finds an item set by its exact name.
     *
     * @param name The name of the item set.
     * @return The {@link ItemSet} entity, or null if not found.
     */
    ItemSet findByName(String name);
}
