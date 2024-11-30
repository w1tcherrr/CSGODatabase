package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.repository.SteamAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service class for managing {@link SteamAccount} entities.
 * Provides methods for retrieving, saving, and querying Steam accounts.
 */
@Component
public class SteamAccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SteamAccountRepository steamAccountRepository;
    private List<String> ids = new ArrayList<>();

    /**
     * Constructor for {@link SteamAccountService}.
     *
     * @param steamAccountRepository The repository for {@link SteamAccount}.
     */
    public SteamAccountService(SteamAccountRepository steamAccountRepository) {
        this.steamAccountRepository = steamAccountRepository;
    }

    /**
     * Initializes the service by fetching unmapped SteamAccount IDs from the database.
     */
    public synchronized void init() {
        LOGGER.info("SteamAccountService#init()");
        ids = new ArrayList<>(steamAccountRepository.findAllUnmappedIDs());
        Collections.shuffle(ids); // Shuffle to ensure random selection.
    }

    /**
     * Retrieves the next set of unmapped SteamAccount IDs.
     *
     * @param amount The number of IDs to retrieve.
     * @return A list of SteamAccount IDs.
     */
    public synchronized List<String> findNextIds(long amount) {
        LOGGER.info("SteamAccountService#findNextIds({})", amount);

        if (ids.size() < 1_000) { // Fetch new IDs if the remaining count is low.
            init();
        }

        if (amount <= 0) {
            return List.of();
        }

        if (amount > ids.size()) {
            throw new IllegalStateException("Insufficient IDs left to supply the mapper.");
        }

        List<String> result = ids.subList(0, (int) amount);
        ids = ids.subList((int) amount, ids.size());
        return result;
    }

    /**
     * Counts the total number of Steam accounts in the database.
     *
     * @return The total count.
     */
    public long count() {
        LOGGER.info("SteamAccountService#count()");
        return steamAccountRepository.count();
    }

    /**
     * Checks if a SteamAccount with the given ID exists in the database.
     *
     * @param current The SteamAccount ID to check.
     * @return {@code true} if the account exists, otherwise {@code false}.
     */
    public boolean containsById64(String current) {
        LOGGER.info("SteamAccountService#containsById64({})", current);
        return steamAccountRepository.containsById64(current);
    }

    /**
     * Saves a list of {@link SteamAccount} entities to the database.
     *
     * @param accountList The list of accounts to save.
     */
    public void saveAll(List<SteamAccount> accountList) {
        LOGGER.info("SteamAccountService#saveAll({})", accountList.size());
        steamAccountRepository.saveAll(accountList);
    }
}
