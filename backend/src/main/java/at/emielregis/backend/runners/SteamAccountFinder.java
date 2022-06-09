package at.emielregis.backend.runners;

import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.data.responses.HttpFriendsResponse;
import at.emielregis.backend.data.responses.HttpInventoryResponse;
import at.emielregis.backend.service.CSGOInventoryService;
import at.emielregis.backend.service.ItemManager;
import at.emielregis.backend.service.SteamAccountService;
import at.emielregis.backend.service.UrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class SteamAccountFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SteamAccountService steamAccountService;
    private final CSGOInventoryService csgoInventoryService;
    private final UrlProvider urlProvider;
    private final RestTemplate restTemplate;
    private final ItemManager itemManager;

    private static final long MAX_ACCOUNTS = 100_000;
    private static final long MAX_POSSIBLE_ACCOUNTS = 250_000;

    private boolean findNewAccounts = true;
    private LocalDateTime locked_until = LocalDateTime.MIN;
    private Integer currentApiCalls = 0;

    public SteamAccountFinder(
        SteamAccountService steamAccountService,
        CSGOInventoryService csgoInventoryService,
        ItemManager itemManager,
        UrlProvider urlProvider
    ) {
        this.steamAccountService = steamAccountService;
        this.csgoInventoryService = csgoInventoryService;
        this.itemManager = itemManager;
        this.urlProvider = urlProvider;
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void searchPlayers() {
        LOGGER.info("STARTING PLAYER SEARCH");
        List<String> playerIDs = getPlayer64IDs();

        for (String id64 : playerIDs) {
            mapUser(id64);
        }

        mapNextPlayers();
    }

    public void mapNextPlayers() {
        LOGGER.info("Mapping next players");

        long alreadyMappedAccounts = steamAccountService.count();
        List<String> nextAccounts = steamAccountService.findNextIds(Math.min(MAX_ACCOUNTS - alreadyMappedAccounts, 25));
        long currentMaxPossibleAccounts = steamAccountService.getMaxPossibleAccounts();

        LOGGER.info("Already mapped {} players", alreadyMappedAccounts);
        LOGGER.info("Currently at {} Api calls", currentApiCalls);
        LOGGER.info("Current max possible accounts: {}", currentMaxPossibleAccounts);
        LOGGER.info("Mapping account with ids: {} next", nextAccounts);

        if (nextAccounts.size() == 0) {
            LOGGER.info("Finished mapping {} accounts.", MAX_ACCOUNTS);
            return;
        }

        if (findNewAccounts && currentMaxPossibleAccounts > MAX_POSSIBLE_ACCOUNTS) {
            LOGGER.info("Not searching for friends anymore, already have enough IDs stored.");
            findNewAccounts = false;
        }

        for (String id64 : nextAccounts) {
            if (currentApiCalls >= 100) {
                lockForApiCalls();
            }
            waitForLock();
            mapUser(id64);
        }

        mapNextPlayers();
    }

    @Transactional
    protected void mapUser(String id64) {
        if (alreadyMapped(id64)) {
            LOGGER.info("Request to map user with id: {} rejected, user already mapped", id64);
            return;
        }

        LOGGER.info("Request to map user with id: {} accepted", id64);

        SteamAccount.SteamAccountBuilder accountBuilder =
            SteamAccount.SteamAccountBuilder.create()
                .withId64(id64);

        if (!mapInventory(accountBuilder, id64)) {
            return;
        }

        if (findNewAccounts) {
            mapFriends(accountBuilder, id64);
        }

        steamAccountService.save(accountBuilder.build());
    }

    private boolean mapInventory(SteamAccount.SteamAccountBuilder accountBuilder, String id64) {
        LOGGER.info("Mapping inventory for user with id: {}", id64);

        HttpInventoryResponse httpInventoryResponse;
        try {
            currentApiCalls++;
            httpInventoryResponse = restTemplate.getForObject(urlProvider.getFirstInventoryRequestUri(id64), HttpInventoryResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 403) {
                accountBuilder.withCSGOInventory(null);
                return true;
            } else if (e.getRawStatusCode() == 429) {
                lockAndCircleKey();
            }
            return false;
        }

        if (httpInventoryResponse == null || !httpInventoryResponse.successful()) {
            return true;
        }

        Map<String, Integer> inventoryMap = httpInventoryResponse.getInventory();
        Map<String, String> typeMap = httpInventoryResponse.getTypes();

        if (httpInventoryResponse.hasMoreItems()) {
            HttpInventoryResponse httpInventoryResponse1;
            try {
                currentApiCalls++;
                httpInventoryResponse1 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse.getLastAssetId()), HttpInventoryResponse.class);
            } catch (RestClientResponseException e) {
                if (e.getRawStatusCode() == 429) {
                    lockAndCircleKey();
                }
                return false;
            }

            if (httpInventoryResponse1 == null) {
                return false;
            }

            combineMaps(inventoryMap, typeMap, httpInventoryResponse1);
        }

        CSGOInventory.CSGOInventoryBuilder builder = CSGOInventory.CSGOInventoryBuilder.create()
            .withItems(itemManager.convert(inventoryMap, typeMap));
        CSGOInventory inventory = csgoInventoryService.save(builder.build());
        accountBuilder.withCSGOInventory(inventory);

        return true;
    }

    private void combineMaps(Map<String, Integer> inventoryMap, Map<String, String> typeMap, HttpInventoryResponse httpInventoryResponse1) {
        httpInventoryResponse1.getInventory().forEach((key, value) -> {
            if (inventoryMap.containsKey(key)) {
                inventoryMap.put(key, inventoryMap.get(key) + value);
            } else {
                inventoryMap.put(key, value);
            }
        });

        httpInventoryResponse1.getTypes().forEach((key, value) -> {
            if (typeMap.containsKey(key) && !typeMap.get(key).equals(value)) {
                LOGGER.warn("ClassID {} with value {} already assigned to value {}", key, value, typeMap.get(key));
            } else {
                typeMap.put(key, value);
            }
        });
    }

    private void mapFriends(SteamAccount.SteamAccountBuilder accountBuilder, String id64) {
        LOGGER.info("Mapping friends for user with id: {}", id64);

        HttpFriendsResponse httpFriendsResponse = null;
        try {
            currentApiCalls++;
            httpFriendsResponse = restTemplate.getForObject(urlProvider.getFriendsRequestUri(id64), HttpFriendsResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 429) {
                lockAndCircleKey();
            }
            accountBuilder.withFriendIds(null);
        }

        if (httpFriendsResponse != null) {
            accountBuilder.withFriendIds(httpFriendsResponse.getFriendId64s());
        } else {
            accountBuilder.withFriendIds(null);
        }
    }

    private void lockForApiCalls() {
        LOGGER.info("100 api calls exceeded - sleeping for one minute and resetting counter to avoid 429.");
        int counter = 0;
        try {
            while (counter++ < 6) {
                Thread.sleep(10_000);
                LOGGER.info("Still waiting.");
            }
            currentApiCalls = 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void lockAndCircleKey() {
        LOGGER.info("Locking for 2 minutes due to 429 error.");
        currentApiCalls = 0;
        locked_until = LocalDateTime.now().plusMinutes(2);
        urlProvider.circleKey();
    }

    public void waitForLock() {
        while (isLocked()) {
            try {
                LOGGER.info("Still locked due to 429.");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLocked() {
        return locked_until.isAfter(LocalDateTime.now());
    }

    private boolean alreadyMapped(String id64) {
        return steamAccountService.containsBy64Id(id64);
    }

    private static List<String> getPlayer64IDs() {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(SteamAccountFinder.class.getClassLoader().getResourceAsStream("user-ids.txt")));
        BufferedReader reader = new BufferedReader(r);
        return reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).toList();
    }
}
