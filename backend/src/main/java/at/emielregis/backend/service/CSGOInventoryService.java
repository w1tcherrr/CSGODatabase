package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.repository.CSGOInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Set;

/**
 * Service for managing CS:GO inventory entities.
 * Provides operations for saving, deleting, and querying inventory data.
 */
@Component
public class CSGOInventoryService {
    private final CSGOInventoryRepository csgoInventoryRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Constructs the service with the provided repository.
     *
     * @param csgoInventoryRepository The repository for managing CS:GO inventories.
     */
    public CSGOInventoryService(CSGOInventoryRepository csgoInventoryRepository) {
        this.csgoInventoryRepository = csgoInventoryRepository;
    }

    /**
     * Saves a CS:GO inventory entity to the database.
     *
     * @param inventory The inventory to save.
     */
    public void save(CSGOInventory inventory) {
        LOGGER.info("Saving CS:GO inventory with ID: {}", inventory.getId());
        csgoInventoryRepository.saveAndFlush(inventory);
    }

    /**
     * Counts the total number of CS:GO inventories in the database.
     *
     * @return The total count of inventories.
     */
    public long count() {
        LOGGER.info("Counting total CS:GO inventories.");
        return csgoInventoryRepository.count();
    }

    /**
     * Deletes all inventories with IDs in the specified set.
     *
     * @param orphanedIDs The IDs of the inventories to delete.
     */
    @Transactional
    public void deleteAllById(Set<Long> orphanedIDs) {
        LOGGER.info("Deleting orphaned inventories: {}", orphanedIDs);
        for (Long id : orphanedIDs) {
            csgoInventoryRepository.deleteById(id);
        }
        csgoInventoryRepository.flush();
    }

    /**
     * Retrieves the IDs of all inventories in the database.
     *
     * @return A set of inventory IDs.
     */
    public Set<Long> getAllInventoryIDs() {
        LOGGER.info("Retrieving all inventory IDs.");
        return csgoInventoryRepository.getAllInvIDs();
    }

    /**
     * Retrieves the IDs of inventories linked to CS:GO accounts.
     *
     * @return A set of inventory IDs linked to accounts.
     */
    public Set<Long> getNormalInventoryIDs() {
        LOGGER.info("Retrieving inventory IDs linked to accounts.");
        return csgoInventoryRepository.getNormalInvIDs();
    }

}
