package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.enums.HttpResponseMappingStatus;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.CSGOInventoryService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.SteamAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    /*
    The max amount of unique CS:GO inventories to be mapped (not the number of accounts since accounts without csgo,
    or private accounts / private inventories are not counted. It is a precise value and is never exceeded.
    The program stops once when it reaches the exact amount. Note that the groups specified in the groups.txt file
    must contain around at least 1.5 times as many accounts as this value!
     */
    private static final long MAX_CSGO_ACCOUNTS = 1_000_000;

    /*
    amount of accounts already mapped that also have an inventory
     */
    private long alreadyMappedAccountsWithInventories;

    /*
    if true all threads will stop mapping inventories immediately
     */
    private boolean stop;

    /*
    MAX_PROXIES -> maximum account of proxies to be read from the proxies.txt file, if MAX_PROXIES = 1 the application only uses one thread for all requests.
    AMOUNT_OF_PROXIES -> The actual amount of used proxies in case there are fewer proxies than specified by MAX_PROXIES. Put MAX_PROXIES to Integer.MAX_VALUE
    if you want all proxies of your file to be used.
     */
    private static final int MAX_PROXIES = 100;
    private static final int AMOUNT_OF_PROXIES = getProxies().size();

    public CSGOAccountMapper(
        CSGOAccountService csgoAccountService,
        CSGOInventoryService csgoInventoryService,
        CSGOInventoryMapper csgoInventoryMapper,
        SteamAccountService steamAccountService,
        SteamGroupMapper steamGroupMapper,
        ItemService itemService) {
        this.csgoAccountService = csgoAccountService;
        this.csgoInventoryService = csgoInventoryService;
        this.csgoInventoryMapper = csgoInventoryMapper;
        this.steamAccountService = steamAccountService;
        this.steamGroupMapper = steamGroupMapper;
        this.itemService = itemService;
    }

    /**
     * Starts the mapping process. Creates a thread for each proxy and maps for players.
     */
    public void start() {
        alreadyMappedAccountsWithInventories = csgoAccountService.countWithInventory();

        LOGGER.info("Starting with: {} inventories", alreadyMappedAccountsWithInventories);

        List<Thread> threads = new ArrayList<>();

        // create a thread for each proxy to run requests from
        for (String[] proxyParams : getProxies()) {
            Thread t = new Thread(() -> {
                // initialize proxy
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyParams[0], Integer.parseInt(proxyParams[1])));
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setProxy(proxy);
                RestTemplate template = new RestTemplate(requestFactory);

                // start mapping csgo inventories
                mapNextPlayers(template);

                // notify the user after termination of the proxy
                LOGGER.info("FINISHED EXECUTION OF THREAD");
            });
            threads.add(t);
            t.start();
        }

        // wait until all threads finish
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // delete all orphaned items after the mapping has stopped.
        deleteOrphanedItems();
    }

    /**
     * Gets an array of the proxies [ip, port]. Reads up to MAX_PROXIES proxies from the file.
     *
     * @return List of proxies.
     */
    private static List<String[]> getProxies() {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(CSGOAccountMapper.class.getClassLoader().getResourceAsStream("proxies.txt")));
        BufferedReader reader = new BufferedReader(r);
        List<String[]> lines = reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).map(line -> line.split(":")).collect(Collectors.toList());
        Collections.shuffle(lines); // shuffle so different proxies are selected each time for rate limiting purposes
        return lines.stream().limit(MAX_PROXIES).toList();
    }

    /**
     * Maps the next valid players from the current thread.
     *
     * @param template The RestTemplate from which to execute the calls.
     */
    public void mapNextPlayers(RestTemplate template) {
        LOGGER.info("Mapping next players");

        steamGroupMapper.findAccounts();

        // getting next steam account ids to map
        List<String> nextAccounts = getIDs();

        LOGGER.info("Already mapped {} players", alreadyMappedAccountsWithInventories);
        LOGGER.info("Mapping account with ids: {} next", nextAccounts);

        // this means that no accounts should be mapped anymore from the thread - this happens when the max is reached
        if (nextAccounts.size() == 0) {
            LOGGER.info("Finished mapping {} accounts.", MAX_CSGO_ACCOUNTS);
            return;
        }

        if (stop) return;

        // map each user individually
        for (String id64 : nextAccounts) {
            mapUser(id64, template);
        }

        if (stop) return;

        // call recursively to map next players
        mapNextPlayers(template);
    }

    /**
     * Gets the next SteamAccount IDs to be mapped by the current thread.
     *
     * @return The list of ids.
     */
    private synchronized List<String> getIDs() {
        long max = MAX_CSGO_ACCOUNTS - alreadyMappedAccountsWithInventories;
        long amount = Math.max(max / AMOUNT_OF_PROXIES, 1);
        amount = Math.min(amount, 100);
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

            if (stop) {
                return;
            }
        }

        /// if an account is already mapped it is not mapped again - due to the way the ids are selected this never happens - this only for debugging reasons if the selection breaks.
        if (alreadyMapped(id64)) {
            LOGGER.info("Request to map user with id: {} rejected, user already mapped", id64);
            return;
        }

        LOGGER.info("Request to map user with id: {} accepted", id64);

        CSGOAccount.CSGOAccountBuilder accountBuilder =
            CSGOAccount.builder()
                .id64(id64);

        // Get the inventory for the user. If any of the needed calls fail the fetcher returns HttpResponseMappingStatus.FAILED. In this case we don't store the account.
        if (csgoInventoryMapper.getInventory(accountBuilder, id64, template) == HttpResponseMappingStatus.FAILED) {
            return;
        }

        // We increase the amount of alreadyMappedAccountsWithInventories if it has an inventory. If this amount is greater than the allowed amount we don't store it and return instead.
        // The program will automatically terminate afterwards since the next id selection will be empty by default. Otherwise we save the inventory and the account.
        synchronized (this) {
            CSGOAccount acc = accountBuilder.build();
            if (acc.getCsgoInventory() != null) {
                if (++alreadyMappedAccountsWithInventories > MAX_CSGO_ACCOUNTS) {
                    --alreadyMappedAccountsWithInventories;
                    return;
                }
                CSGOInventory inv = acc.getCsgoInventory();
                itemService.saveAll(inv.getItems());
                csgoInventoryService.save(acc.getCsgoInventory());
            }
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
    private void deleteOrphanedItems() {
        // use sets not lists for performance as ids may not be duplicate anyway
        LOGGER.info("Delete orphaned items");
        Set<Long> allItemIDs = itemService.getAllItemIDs();
        Set<Long> normalItemIDs = itemService.getNormalItemIDs();
        Set<Long> orphanedIDs = new HashSet<>();

        for (Long id : allItemIDs) {
            if (!normalItemIDs.contains(id)) {
                orphanedIDs.add(id);
            }
        }

        LOGGER.info("Total items before delete: " + itemService.count());
        LOGGER.info("Total orphaned items: " + orphanedIDs.size());
        itemService.deleteAllById(orphanedIDs);
        LOGGER.info("Total items after delete: " + itemService.count());
    }
}
