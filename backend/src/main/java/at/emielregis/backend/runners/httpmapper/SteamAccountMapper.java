package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.data.enums.HttpResponseMappingStatus;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.CSGOInventoryService;
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
import java.util.List;
import java.util.Objects;

@Component
public class SteamAccountMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CSGOAccountService csgoAccountService;
    private final CSGOInventoryService csgoInventoryService;
    private final CSGOInventoryMapper csgoInventoryMapper;
    private final SteamAccountService steamAccountService;
    private final SteamGroupMapper steamGroupMapper;

    private static final long MAX_CSGO_ACCOUNTS = 50;
    private static final long MAX_STEAM_ACCOUNTS = 1_000; // this should be at least twice the above amount - since not all steam accounts have csgo

    private long alreadyMappedAccountsWithInventories = 0;
    private boolean stop;

    private static final int AMOUNT_OF_THREADS = getProxies().size();
    private static final int MAX_PROXIES = 10;

    public SteamAccountMapper(
        CSGOAccountService csgoAccountService,
        CSGOInventoryService csgoInventoryService, CSGOInventoryMapper csgoInventoryMapper,
        SteamAccountService steamAccountService,
        SteamGroupMapper steamGroupMapper) {
        this.csgoAccountService = csgoAccountService;
        this.csgoInventoryService = csgoInventoryService;
        this.csgoInventoryMapper = csgoInventoryMapper;
        this.steamAccountService = steamAccountService;
        this.steamGroupMapper = steamGroupMapper;
    }

    public void run() {
        alreadyMappedAccountsWithInventories = csgoAccountService.countWithInventory();
        for (String[] proxyParams : getProxies()) {
            new Thread(() -> {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyParams[0], Integer.parseInt(proxyParams[1])));
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setProxy(proxy);
                RestTemplate template = new RestTemplate(requestFactory);
                steamGroupMapper.findAccounts(getSteamGroups(), MAX_STEAM_ACCOUNTS);
                mapNextPlayers(template);
                LOGGER.info("FINISHED EXECUTION OF THREAD");
            }).start();
        }
    }

    private static List<String[]> getProxies() {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(SteamAccountMapper.class.getClassLoader().getResourceAsStream("proxies.txt")));
        BufferedReader reader = new BufferedReader(r);
        return reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).map(line -> line.split(":")).limit(MAX_PROXIES).toList();
    }

    private static List<String> getSteamGroups() {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(SteamAccountMapper.class.getClassLoader().getResourceAsStream("groups.txt")));
        BufferedReader reader = new BufferedReader(r);
        return reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).toList();
    }

    public void mapNextPlayers(RestTemplate template) {
        LOGGER.info("Mapping next players");

        List<String> nextAccounts;
        synchronized (this) {
            nextAccounts = getIDs();
        }

        LOGGER.info("Already mapped {} players", alreadyMappedAccountsWithInventories);
        LOGGER.info("Mapping account with ids: {} next", nextAccounts);

        if (nextAccounts.size() == 0) {
            LOGGER.info("Finished mapping {} accounts.", MAX_CSGO_ACCOUNTS);
            return;
        }

        for (String id64 : nextAccounts) {
            mapUser(id64, template);
        }

        if (stop) return;

        mapNextPlayers(template);
    }

    private List<String> getIDs() {
        long max = MAX_CSGO_ACCOUNTS - alreadyMappedAccountsWithInventories;
        long amount = Math.max(max / AMOUNT_OF_THREADS, 1);
        amount = Math.min(amount, 100);
        if (alreadyMappedAccountsWithInventories + amount > MAX_CSGO_ACCOUNTS) {
            amount = MAX_CSGO_ACCOUNTS - alreadyMappedAccountsWithInventories;
        }
        if (amount <= 0) {
            return List.of();
        }
        return steamAccountService.findNextIds(amount);
    }

    @Transactional
    protected void mapUser(String id64, RestTemplate template) {
        synchronized (this) {
            if (alreadyMappedAccountsWithInventories >= MAX_CSGO_ACCOUNTS) {
                stop = true;
                return;
            }

            if (stop) {
                return;
            }
        }

        if (alreadyMapped(id64)) {
            LOGGER.info("Request to map user with id: {} rejected, user already mapped", id64);
            return;
        }

        LOGGER.info("Request to map user with id: {} accepted", id64);

        CSGOAccount.CSGOAccountBuilder accountBuilder =
            CSGOAccount.builder()
                .id64(id64);

        if (csgoInventoryMapper.getInventory(accountBuilder, id64, template) == HttpResponseMappingStatus.FAILED) {
            return;
        }

        synchronized (this) {
            CSGOAccount acc = accountBuilder.build();
            if (acc.getCsgoInventory() != null) {
                if (++alreadyMappedAccountsWithInventories > MAX_CSGO_ACCOUNTS) {
                    --alreadyMappedAccountsWithInventories;
                    return;
                }
                csgoInventoryService.save(acc.getCsgoInventory());
            }
            csgoAccountService.save(acc);
        }
    }

    private boolean alreadyMapped(String id64) {
        return csgoAccountService.containsById64(id64);
    }
}
