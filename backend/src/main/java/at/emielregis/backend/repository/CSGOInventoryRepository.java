package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.CSGOInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Repository interface for {@link CSGOInventory} entities.
 * Provides methods for managing and querying CSGO inventories.
 */
public interface CSGOInventoryRepository extends JpaRepository<CSGOInventory, Long> {

    /**
     * Retrieves the IDs of all normal inventories linked to CSGO accounts.
     *
     * @return A set of IDs for normal CSGO inventories.
     */
    @Query("SELECT inv.id FROM CSGOAccount acc JOIN acc.csgoInventory inv")
    Set<Long> getNormalInvIDs();

    /**
     * Retrieves the IDs of all CSGO inventories in the database.
     *
     * @return A set of all CSGO inventory IDs.
     */
    @Query("SELECT inv.id FROM CSGOInventory inv")
    Set<Long> getAllInvIDs();

    /**
     * Deletes a CSGO inventory by its ID.
     *
     * @param id The ID of the inventory to delete.
     */
    @Modifying
    @Query("DELETE FROM CSGOInventory i WHERE i.id = :id")
    void deleteById(@Param("id") Long id);
}
