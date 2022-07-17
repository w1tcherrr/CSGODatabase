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

@Component
public class SteamAccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SteamAccountRepository steamAccountRepository;
    private boolean initialized = false;
    private List<String> ids = new ArrayList<>();

    public SteamAccountService(SteamAccountRepository steamAccountRepository) {
        this.steamAccountRepository = steamAccountRepository;
    }

    public synchronized void init() {
        LOGGER.info("SteamAccountService#init()");
        ids = new ArrayList<>(steamAccountRepository.findAllUnmappedIDs());
        Collections.shuffle(ids); // shuffle once so random accounts are picked and not only those found first
    }

    /**
     * Finds the next amount ids that have not been mapped to a CSGOAccount yet.
     *
     * @param amount The amount of accounts.
     * @return The List of account id64s.
     */
    public synchronized List<String> findNextIds(long amount) {
        LOGGER.info("SteamAccountService#findNextIds(" + amount + ")");
        if (!initialized) { // initialize on the first call
            initialized = true;
            init();
        }
        if (ids.size() < 1_000) { // if not enough ids are left fetch all new ids
            init();
        }
        if (amount < 0) {
            return List.of();
        }
        if (amount > ids.size()) { // this means that the groups do not contain enough accounts. In that case the program is terminated early.
            throw new IllegalStateException("There are not enough ids left to supply the mapper. Your Steam-Account to CSGO-Account Ratio is wrong.");
        }
        List<String> result = ids.subList(0, (int) amount);
        ids = ids.subList((int) amount, ids.size());
        return result;
    }

    public long count() {
        LOGGER.info("SteamAccountService#count()");
        return steamAccountRepository.count();
    }

    public boolean containsById64(String current) {
        LOGGER.info("SteamAccountService#containsById64(" + current + ")");
        return steamAccountRepository.containsById64(current);
    }

    public void saveAll(List<SteamAccount> accountList) {
        LOGGER.info("SteamAccountService#saveAll()");
        steamAccountRepository.saveAll(accountList);
    }
}
