package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.enums.HttpResponseMappingStatus;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.CSGOInventoryService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.ProxyService;
import at.emielregis.backend.service.SteamAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used to map CSGOAccounts by first searching for valid accounts from
 * the Steam Groups specified in the groups.txt file. It then maps the accounts and their
 * respective CS:GO inventories and stores them in a database.
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

    /*
    The max amount of unique CS:GO inventories to be mapped (not the number of accounts since accounts without csgo,
    or private accounts / private inventories are not counted. It is a precise value and is never exceeded.
    The program stops once when it reaches the exact amount. Note that the groups specified in the groups.txt file
    must contain around at least 1.5 times as many accounts as this value!

    MIN_ITEMS_FOR_ACCOUNT specifies how many items an inventory must have to be stored. This is to filter out most
    very obvious smurf accounts, which only have extremely few items.
     */
    @Value("${user-properties.max-csgo-accounts}")
    private long MAX_CSGO_ACCOUNTS;

    @Value("${user-properties.max-accounts-for-session}")
    private long MAX_ACCOUNTS_FOR_SESSION;

    @Value("${user-properties.min-items-per-account}")
    private long MIN_ITEMS_FOR_ACCOUNT;

    @Value("${user-properties.max-ids-per-batch}")
    private int MAX_IDS_PER_BATCH;

    @Value("${user-properties.amount-of-threads}")
    private int AMOUNT_OF_THREADS;

    @Value("${user-properties.amount-of-proxies}")
    private int AMOUNT_OF_PROXIES;

    /*
    amount of accounts already mapped
     */
    private long alreadyMappedAccounts;

    /*
    amount of accounts already mapped that also have an inventory
     */
    private long alreadyMappedAccountsWithInventories;

    /*
    if true all threads will stop mapping inventories immediately
     */
    private volatile boolean stop;

    public CSGOAccountMapper(
        CSGOAccountService csgoAccountService,
        CSGOInventoryService csgoInventoryService,
        CSGOInventoryMapper csgoInventoryMapper,
        SteamAccountService steamAccountService,
        SteamGroupMapper steamGroupMapper,
        ItemService itemService,
        ProxyService proxyService) {
        this.csgoAccountService = csgoAccountService;
        this.csgoInventoryService = csgoInventoryService;
        this.csgoInventoryMapper = csgoInventoryMapper;
        this.steamAccountService = steamAccountService;
        this.steamGroupMapper = steamGroupMapper;
        this.itemService = itemService;
        this.proxyService = proxyService;
    }

    /**
     * Starts the mapping process. Creates a thread for each proxy and maps for players.
     */
    public void start() {
        alreadyMappedAccountsWithInventories = csgoAccountService.countWithInventory();

        LOGGER.info("Starting with: {} inventories", alreadyMappedAccountsWithInventories);

        proxyService.addThreads(AMOUNT_OF_THREADS, AMOUNT_OF_PROXIES,
            templates -> {
                while (!stop) {
                    // start mapping csgo inventories
                    mapNextPlayers(templates);
                }

                // notify the user after termination of the proxy
                LOGGER.info("FINISHED EXECUTION OF THREAD");
            });

        proxyService.await();

        // delete all orphaned items once after the mapping has stopped.
        if (MAX_ACCOUNTS_FOR_SESSION >= MAX_CSGO_ACCOUNTS) {
            deleteOrphanedItems();
            deleteOrphanedInventories();
        }
    }

    /**
     * Maps the next valid players from the current thread.
     *
     * @param templates The RestTemplates from which to execute the calls.
     */
    public void mapNextPlayers(RestTemplate[] templates) {
        LOGGER.info("Mapping next players");
        int proxyIndex = 0;

        steamGroupMapper.findAccounts(templates);

        // getting next steam account ids to map
        List<String> nextAccounts = getIDs();

        LOGGER.info("Already mapped {} players", alreadyMappedAccountsWithInventories);
        LOGGER.info("Mapping account with ids: {} next", nextAccounts);

        // this means that no accounts should be mapped anymore from the thread - this happens when the max is reached
        if (nextAccounts.size() == 0) {
            LOGGER.info("Finished mapping {} accounts.", MAX_CSGO_ACCOUNTS);
            stop = true;
            return;
        }

        // map each user individually
        for (String id64 : nextAccounts) {
            if (stop) {
                break;
            }

            mapUser(id64, templates[proxyIndex]);
            proxyIndex = (proxyIndex + 1) % templates.length;
        }
    }

    /**
     * Gets the next SteamAccount IDs to be mapped by the current thread.
     *
     * @return The list of ids.
     */
    private synchronized List<String> getIDs() {
        long max = MAX_CSGO_ACCOUNTS - alreadyMappedAccountsWithInventories;
        long amount = Math.max(max / proxyService.maxThreads(), 1);
        amount = Math.min(amount, MAX_IDS_PER_BATCH);
        if (alreadyMappedAccountsWithInventories + amount > MAX_CSGO_ACCOUNTS) {
            amount = MAX_CSGO_ACCOUNTS - alreadyMappedAccountsWithInventories;
        }
        if (amount <= 0) {
            return List.of();
        }
        return steamAccountService.findNextIds(amount);
    }

    /**
     * Maps a single SteamAccount to create a CSGOAccount for that user.
     *
     * @param id64     The id of the user.
     * @param template The RestTemplate from which to send http calls.
     */
    @Transactional
    protected void mapUser(String id64, RestTemplate template) {

        // if the amount of mapped inventories is greater or equal to the max amount the mapping is stopped in all threads
        synchronized (this) {
            if (alreadyMappedAccountsWithInventories >= MAX_CSGO_ACCOUNTS) {
                stop = true;
                return;
            }
            if (alreadyMappedAccounts >= MAX_ACCOUNTS_FOR_SESSION) {
                LOGGER.info("MAX ACCOUNTS FOR SESSION REACHED - TERMINATING");
                stop = true;
                return;
            }
        }

        if (stop) {
            return;
        }

        /*
         if an account is already mapped it is not mapped again
         this can only happen when some threads fetch new steam accounts and the init method is called again in the
         steam-account-service due to too little ids being left
         */
        if (alreadyMapped(id64)) {
            LOGGER.info("Request to map user with id: {} rejected, user already mapped", id64);
            return;
        }

        LOGGER.info("Request to map user with id: {} accepted", id64);

        CSGOAccount.CSGOAccountBuilder accountBuilder =
            CSGOAccount.builder()
                .id64(id64);

        // Get the inventory for the user. If any of the needed calls fail the fetcher returns HttpResponseMappingStatus.TOO_MANY_REQUESTS. In this case we don't store the account.
        if (csgoInventoryMapper.getInventory(accountBuilder, id64, template) == HttpResponseMappingStatus.TOO_MANY_REQUESTS) {
            return;
        }

        // We increase the amount of alreadyMappedAccountsWithInventories if it has an inventory. If this amount is greater than the allowed amount we don't store it and return instead.
        // The program will automatically terminate afterwards since the next id selection will be empty by default. Otherwise we save the inventory and the account.
        synchronized (this) {

            // check this right before saving - we don't want duplicate accounts to be saved.
            // they should not be saved either way, but it is better to check again
            if (alreadyMapped(id64)) {
                LOGGER.info("Request to map user with id: {} rejected, user already mapped", id64);
                return;
            }

            CSGOAccount acc = accountBuilder.build();
            if (acc.getCsgoInventory() != null) {
                if (++alreadyMappedAccountsWithInventories > MAX_CSGO_ACCOUNTS) {
                    --alreadyMappedAccountsWithInventories;
                    return;
                }
                if (acc.getCsgoInventory().getTotalItemAmount() >= MIN_ITEMS_FOR_ACCOUNT) {
                    CSGOInventory inv = acc.getCsgoInventory();
                    inv.setItemCollections(itemService.convert(inv.getItemCollections()));
                    itemService.saveAll(inv.getItemCollections());
                    csgoInventoryService.save(acc.getCsgoInventory());
                } else {
                    LOGGER.info("Inventory does not have enough items and will not be stored.");
                    acc.setCsgoInventory(null);
                    --alreadyMappedAccountsWithInventories;
                }
            }

            ++alreadyMappedAccounts;
            csgoAccountService.save(acc);
        }
    }

    /**
     * Returns whether the SteamAccount with the id64 has already been mapped into a CsgoAccount.
     *
     * @param id64 The id64 of the steam account.
     * @return True if it was already mapped, false otherwise.
     */
    private boolean alreadyMapped(String id64) {
        return csgoAccountService.containsById64(id64);
    }

    /**
     * Deletes all orphaned items - that is items that stored that do not belong to an inventory.
     * This happens when the application is forcefully stopped while storing the items of an inventory
     * but not having stored the inventory itself yet.
     * <p>
     * This method takes long and should only be run once after the whole mapping is done.
     */
    @Transactional
    protected void deleteOrphanedItems() {
        // use sets not lists for performance as ids may not be duplicate anyway
        LOGGER.info("Delete orphaned items");

        Set<Long> orphanedIDs = itemService.getOrphanedIDs();

        if (orphanedIDs.size() == 0) {
            LOGGER.info("No orphaned items!");
            return;
        }

        LOGGER.info("Total items before delete: " + itemService.count());
        LOGGER.info("Total orphaned items: " + orphanedIDs.size());
        //itemService.deleteAllById(orphanedIDs);
        LOGGER.info("Total items after delete: " + itemService.count());
    }

    /**
     * Deletes all orphaned inventories (and items stored in them)
     * This happens when the application is forcefully stopped while storing the inventory
     * but not having stored the CSGOAccount with the inventory yet.
     * <p>
     * This method takes long and should only be run once after the whole mapping is done.
     */
    @Transactional
    protected void deleteOrphanedInventories() {
        // use sets not lists for performance as ids may not be duplicate anyway
        LOGGER.info("Delete orphaned inventories");

        if (csgoInventoryService.count() == csgoInventoryService.getNormalInventoryCount()) {
            LOGGER.info("No orphaned inventories!");
            return;
        }

        Set<Long> allInventoryIDs = csgoInventoryService.getAllInventoryIDs();
        Set<Long> normalInventoryIDs = csgoInventoryService.getNormalInventoryIDs();
        Set<Long> orphanedIDs = new HashSet<>();

        for (Long id : allInventoryIDs) {
            if (!normalInventoryIDs.contains(id)) {
                orphanedIDs.add(id);
            }
        }

        LOGGER.info("Total inventories before delete: " + csgoInventoryService.count());
        LOGGER.info("Total orphaned inventories: " + orphanedIDs.size());
        csgoInventoryService.deleteAllById(orphanedIDs);
        LOGGER.info("Total inventories after delete: " + csgoInventoryService.count());
    }
}
