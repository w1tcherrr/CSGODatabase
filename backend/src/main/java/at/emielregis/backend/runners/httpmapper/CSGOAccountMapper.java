package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.enums.HttpResponseMappingStatus;
import at.emielregis.backend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Handles the mapping of CSGO accounts and their inventories.
 * The accounts are fetched from Steam groups, validated, and stored in the database.
 * The mapping process includes handling proxy requests, inventory retrieval, and cleanup of orphaned items and inventories.
 */
@Component
public class CSGOAccountMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CSGOAccountService csgoAccountService;
    private final CSGOInventoryService csgoInventoryService;
    private final CSGOInventoryMapper csgoInventoryMapper;
    private final SteamAccountService steamAccountService;
    private final SteamGroupMapper steamGroupMapper;
    private final ItemService itemService;
    private final ProxyService proxyService;
    private final BusyWaitingService busyWaitingService;

    // Queue for accounts to be persisted
    private final List<CSGOAccount> accountsToPersist = Collections.synchronizedList(new ArrayList<>());

    @Value("${user-properties.max-csgo-accounts}")
    private long MAX_CSGO_ACCOUNTS; // Max allowed mapped inventories

    @Value("${user-properties.max-accounts-for-session}")
    private long MAX_ACCOUNTS_FOR_SESSION; // Max accounts allowed per session

    @Value("${user-properties.min-items-per-account}")
    private long MIN_ITEMS_FOR_ACCOUNT; // Minimum items required for inventory to be stored

    @Value("${user-properties.max-ids-per-batch}")
    private int MAX_IDS_PER_BATCH; // Max IDs to process in a batch

    @Value("${user-properties.amount-of-threads}")
    private int AMOUNT_OF_THREADS; // Number of threads to use

    @Value("${user-properties.amount-of-proxies}")
    private int AMOUNT_OF_PROXIES; // Number of proxies to use

    private long alreadyMappedAccounts = 0; // Tracks mapped accounts
    private long alreadyMappedAccountsWithInventories = 0; // Tracks mapped accounts with inventories
    private volatile boolean stop = false; // Flag to stop all threads

    public CSGOAccountMapper(
        CSGOAccountService csgoAccountService,
        CSGOInventoryService csgoInventoryService,
        CSGOInventoryMapper csgoInventoryMapper,
        SteamAccountService steamAccountService,
        SteamGroupMapper steamGroupMapper,
        ItemService itemService,
        ProxyService proxyService,
        BusyWaitingService busyWaitingService) {
        this.csgoAccountService = csgoAccountService;
        this.csgoInventoryService = csgoInventoryService;
        this.csgoInventoryMapper = csgoInventoryMapper;
        this.steamAccountService = steamAccountService;
        this.steamGroupMapper = steamGroupMapper;
        this.itemService = itemService;
        this.proxyService = proxyService;
        this.busyWaitingService = busyWaitingService;
    }

    /**
     * Starts the mapping process for CSGO accounts and their inventories.
     * Handles threads for account processing and proxy management.
     */
    public void start() {
        alreadyMappedAccountsWithInventories = csgoAccountService.countWithInventory();
        LOGGER.info("Starting with {} mapped inventories.", alreadyMappedAccountsWithInventories);

        if (MAX_ACCOUNTS_FOR_SESSION <= 0 || alreadyMappedAccounts >= MAX_CSGO_ACCOUNTS) {
            return;
        }

        // Thread for processing persisted accounts
        proxyService.addEmptyThread(() -> {
            while (!stop || !accountsToPersist.isEmpty()) {
                if (accountsToPersist.isEmpty()) {
                    busyWaitingService.wait(5);
                    continue;
                }

                LOGGER.info("Processing accounts in queue: {}", accountsToPersist.size());
                CSGOAccount acc = accountsToPersist.get(0);

                if (alreadyMapped(acc.getId64())) {
                    accountsToPersist.remove(0);
                    continue;
                }

                if (acc.getCsgoInventory() != null) {
                    if (++alreadyMappedAccountsWithInventories > MAX_CSGO_ACCOUNTS) {
                        --alreadyMappedAccountsWithInventories;
                        return;
                    }

                    // Process valid inventories
                    if (acc.getCsgoInventory().getTotalItemAmount() >= MIN_ITEMS_FOR_ACCOUNT) {
                        CSGOInventory inv = acc.getCsgoInventory();
                        inv.setItemCollections(itemService.convert(inv.getItemCollections()));
                        itemService.saveAll(inv.getItemCollections());
                        csgoInventoryService.save(acc.getCsgoInventory());
                    } else {
                        LOGGER.info("Inventory does not meet the minimum item requirement.");
                        acc.setCsgoInventory(null);
                        --alreadyMappedAccountsWithInventories;
                    }
                }

                ++alreadyMappedAccounts;
                csgoAccountService.save(acc);
                accountsToPersist.remove(0);
            }
        });

        // Threads for proxy-based account mapping
        proxyService.addRestTemplateConsumerThreads(AMOUNT_OF_THREADS, AMOUNT_OF_PROXIES,
            templates -> {
                while (!stop) {
                    mapNextPlayers(templates);
                }
                LOGGER.info("Thread execution complete.");
            });

        proxyService.await();

        // Cleanup orphaned items and inventories after mapping
        if (MAX_ACCOUNTS_FOR_SESSION >= MAX_CSGO_ACCOUNTS) {
            deleteOrphanedItems();
            deleteOrphanedInventories();
        }
    }

    /**
     * Maps the next batch of players' accounts using proxies.
     *
     * @param templates The RestTemplate instances for sending HTTP requests.
     */
    public void mapNextPlayers(RestTemplate[] templates) {
        LOGGER.info("Mapping next batch of players.");
        int proxyIndex = 0;

        steamGroupMapper.findAccounts(templates);
        List<String> nextAccounts = getIDs();

        if (nextAccounts.isEmpty()) {
            LOGGER.info("Mapping complete. Maximum accounts reached.");
            stop = true;
            return;
        }

        for (String id64 : nextAccounts) {
            if (stop) break;

            mapUser(id64, templates[proxyIndex]);
            proxyIndex = (proxyIndex + 1) % templates.length;
        }
    }

    /**
     * Retrieves the next set of SteamAccount IDs to map.
     *
     * @return A list of account IDs.
     */
    private List<String> getIDs() {
        long remaining = MAX_CSGO_ACCOUNTS - alreadyMappedAccountsWithInventories;
        long batchAmount = Math.min(Math.max(remaining / proxyService.maxThreads(), 1), MAX_IDS_PER_BATCH);

        if (batchAmount <= 0) {
            return List.of();
        }

        return steamAccountService.findNextIds(batchAmount);
    }

    /**
     * Maps a specific user account to create a CSGOAccount and associated inventory.
     *
     * @param id64     The SteamID64 of the user.
     * @param template The RestTemplate for HTTP calls.
     */
    @Transactional
    public void mapUser(String id64, RestTemplate template) {
        if (alreadyMappedAccountsWithInventories >= MAX_CSGO_ACCOUNTS || stop) {
            stop = true;
            return;
        }

        if (alreadyMappedAccounts >= MAX_ACCOUNTS_FOR_SESSION) {
            LOGGER.info("Session limit reached.");
            stop = true;
            return;
        }

        if (alreadyMapped(id64)) {
            LOGGER.info("Account already mapped: {}", id64);
            return;
        }

        LOGGER.info("Mapping account: {}", id64);

        CSGOAccount.CSGOAccountBuilder accountBuilder = CSGOAccount.builder().id64(id64);

        if (csgoInventoryMapper.getInventory(accountBuilder, id64, template) != HttpResponseMappingStatus.SUCCESS) {
            return;
        }

        if (alreadyMapped(id64)) {
            return;
        }

        accountsToPersist.add(accountBuilder.build());
    }

    /**
     * Checks if an account has already been mapped.
     *
     * @param id64 The SteamID64 of the account.
     * @return True if mapped, otherwise false.
     */
    private boolean alreadyMapped(String id64) {
        return csgoAccountService.containsById64(id64);
    }

    /**
     * Deletes orphaned items from the database.
     */
    @Transactional
    public void deleteOrphanedItems() {
        LOGGER.info("Deleting orphaned items.");

        Set<Long> orphanedIDs = itemService.getOrphanedIDs();

        if (orphanedIDs.isEmpty()) {
            LOGGER.info("No orphaned items found.");
            return;
        }

        LOGGER.info("Found {} orphaned items.", orphanedIDs.size());
        // Uncomment if needed: itemService.deleteAllById(orphanedIDs);
    }

    /**
     * Deletes orphaned inventories from the database.
     */
    @Transactional
    public void deleteOrphanedInventories() {
        LOGGER.info("Deleting orphaned inventories.");

        Set<Long> allInventoryIDs = csgoInventoryService.getAllInventoryIDs();
        Set<Long> normalInventoryIDs = csgoInventoryService.getNormalInventoryIDs();
        Set<Long> orphanedIDs = new HashSet<>(allInventoryIDs);

        orphanedIDs.removeAll(normalInventoryIDs);

        if (orphanedIDs.isEmpty()) {
            LOGGER.info("No orphaned inventories found.");
            return;
        }

        LOGGER.info("Found {} orphaned inventories.", orphanedIDs.size());
        csgoInventoryService.deleteAllById(orphanedIDs);
    }
}
