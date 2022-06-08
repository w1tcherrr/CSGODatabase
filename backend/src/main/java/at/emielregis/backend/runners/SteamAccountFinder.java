package at.emielregis.backend.runners;

import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.data.responses.HttpFriendsResponse;
import at.emielregis.backend.data.responses.HttpGameResponse;
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

    private static final long MAX_ACCOUNTS = 10_000;
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

        if (currentMaxPossibleAccounts > MAX_POSSIBLE_ACCOUNTS) {
            LOGGER.info("Not searching for friends anymore, already have enough IDs stored.");
            findNewAccounts = false;
        }

        if (currentApiCalls >= 100) {
            lockForApiCalls();
        }

        for (String id64 : nextAccounts) {
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
                .withId64(id64)
                .withPrivateFriends(false)
                .withPrivateGames(false)
                .withHasCsgo(false);

        mapGames(accountBuilder, id64);

        if (!mapInventory(accountBuilder, id64)) {
            return;
        }

        if (findNewAccounts) {
            mapFriends(accountBuilder, id64);
        }

        steamAccountService.save(accountBuilder.build());
    }

    private void mapGames(SteamAccount.SteamAccountBuilder accountBuilder, String id64) {
        LOGGER.info("Mapping games for user with id: {}", id64);

        HttpGameResponse httpGameResponse = null;
        try {
            currentApiCalls++;
            httpGameResponse = restTemplate.getForObject(urlProvider.getGamesRequest(id64), HttpGameResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 429) {
                lockAndCircleKey();
                return;
            }
            accountBuilder.withPrivateGames(true);
            accountBuilder.withHasCsgo(false);
        }

        if (httpGameResponse != null) {
            accountBuilder.withPrivateGames(httpGameResponse.hasGamesPrivate());
            accountBuilder.withHasCsgo(httpGameResponse.hasCsgo());
        }
    }

    private boolean mapInventory(SteamAccount.SteamAccountBuilder accountBuilder, String id64) {
        LOGGER.info("Mapping inventory for user with id: {}", id64);

        HttpInventoryResponse httpInventoryResponse = null;
        try {
            currentApiCalls++;
            httpInventoryResponse = restTemplate.getForObject(urlProvider.getFirstInventoryRequestUri(id64), HttpInventoryResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 403) {
                accountBuilder.withCSGOInventory(null);
            } else if (e.getRawStatusCode() == 429) {
                lockAndCircleKey();
            } else {
                return false;
            }
        }

        if (httpInventoryResponse != null) {
            if (!httpInventoryResponse.successful()) {
                return false;
            }
            Map<String, Integer> inventoryMap = httpInventoryResponse.getInventory();
            Map<String, String> typeMap = httpInventoryResponse.getTypes();

            if (httpInventoryResponse.hasMoreItems()) {
                HttpInventoryResponse httpInventoryResponse1 = null;
                try {
                    currentApiCalls++;
                    httpInventoryResponse1 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse.getLastAssetId()), HttpInventoryResponse.class);
                } catch (RestClientResponseException e) {
                    if (e.getRawStatusCode() == 429) {
                        lockAndCircleKey();
                    }
                }
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

            CSGOInventory.CSGOInventoryBuilder builder = CSGOInventory.CSGOInventoryBuilder.create();
            if (inventoryMap != null) {
                builder.withItems(itemManager.convert(inventoryMap, typeMap));
                CSGOInventory inventory = csgoInventoryService.save(builder.build());
                accountBuilder.withCSGOInventory(inventory);
            } else {
                LOGGER.info("Not converting inventory, it is empty");
            }
        }

        return true;
    }

    private void mapFriends(SteamAccount.SteamAccountBuilder accountBuilder, String id64) {
        LOGGER.info("Mapping friends for user with id: {}", id64);

        HttpFriendsResponse httpFriendsResponse = null;
        try {
            currentApiCalls++;
            httpFriendsResponse = restTemplate.getForObject(urlProvider.getFriendsRequestUri(id64), HttpFriendsResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 401) {
                accountBuilder.withPrivateFriends(true);
            } else if (e.getRawStatusCode() == 429) {
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
        LOGGER.info("100 api calls exceeded - sleeping for two minutes and resetting counter to avoid 429.");
        int counter = 0;
        try {
            while(counter++ < 12) {
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
