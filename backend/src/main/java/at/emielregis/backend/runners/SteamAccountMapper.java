package at.emielregis.backend.runners;

import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.service.SteamAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;

@Component
public class SteamAccountMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SteamAccountService steamAccountService;

    private final CSGOInventoryMapper csgoInventoryMapper;
    private final SteamFriendsMapper steamFriendsMapper;

    private static final long MAX_ACCOUNTS = 100_000;
    private static final long MAX_POSSIBLE_ACCOUNTS = 150_000;

    private boolean findNewAccounts = true;

    public SteamAccountMapper(
        SteamAccountService steamAccountService,
        CSGOInventoryMapper csgoInventoryMapper,
        SteamFriendsMapper steamFriendsMapper
    ) {
        this.steamAccountService = steamAccountService;
        this.csgoInventoryMapper = csgoInventoryMapper;
        this.steamFriendsMapper = steamFriendsMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void searchPlayers() {
        List<String> playerIDs = getPlayer64IDs();

        for (String id64 : playerIDs) {
            mapUser(id64);
        }

        mapNextPlayers();
    }

    public void mapNextPlayers() {
        LOGGER.info("Mapping next players");

        long alreadyMappedAccounts = steamAccountService.count();
        List<String> nextAccounts = steamAccountService.findNextIds(Math.min(MAX_ACCOUNTS - alreadyMappedAccounts, 100));
        long currentMaxPossibleAccounts = steamAccountService.getMaxPossibleAccounts();

        LOGGER.info("Already mapped {} players", alreadyMappedAccounts);
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

        if (!csgoInventoryMapper.getInventory(accountBuilder, id64)) {
            return;
        }

        if (findNewAccounts) {
            if (!steamFriendsMapper.mapFriends(accountBuilder, id64)) {
                return;
            }
        }

        steamAccountService.save(accountBuilder.build());
    }

    private boolean alreadyMapped(String id64) {
        return steamAccountService.containsBy64Id(id64);
    }

    private static List<String> getPlayer64IDs() {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(SteamAccountMapper.class.getClassLoader().getResourceAsStream("user-ids.txt")));
        BufferedReader reader = new BufferedReader(r);
        return reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).toList();
    }
}
