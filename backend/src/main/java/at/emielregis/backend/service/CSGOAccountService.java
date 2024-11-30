package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.repository.CSGOAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * Service for managing CS:GO account entities.
 * Provides operations for saving, querying, and checking account data.
 */
@Component
public class CSGOAccountService {
    private final CSGOAccountRepository csgoAccountRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Constructs the service with the provided repository.
     *
     * @param csgoAccountRepository The repository for managing CS:GO accounts.
     */
    public CSGOAccountService(CSGOAccountRepository csgoAccountRepository) {
        this.csgoAccountRepository = csgoAccountRepository;
    }

    /**
     * Saves a CS:GO account entity to the database.
     *
     * @param account The account to save.
     */
    public void save(CSGOAccount account) {
        LOGGER.info("Saving CS:GO account: {}", account.getId64());
        csgoAccountRepository.saveAndFlush(account);
    }

    /**
     * Checks if an account with the given Steam ID64 exists in the database.
     *
     * @param id64 The Steam ID64 of the account.
     * @return True if the account exists, false otherwise.
     */
    public boolean containsById64(String id64) {
        LOGGER.info("Checking if account exists for ID64: {}", id64);
        return csgoAccountRepository.containsById64(id64);
    }

    /**
     * Counts the number of accounts with linked inventories.
     *
     * @return The total count of accounts with inventories.
     */
    public long countWithInventory() {
        LOGGER.info("Counting accounts with inventories.");
        return csgoAccountRepository.countWithInventory();
    }

    /**
     * Counts the total number of CS:GO accounts in the database.
     *
     * @return The total count of accounts.
     */
    public Long count() {
        LOGGER.info("Counting total CS:GO accounts.");
        return csgoAccountRepository.count();
    }
}
