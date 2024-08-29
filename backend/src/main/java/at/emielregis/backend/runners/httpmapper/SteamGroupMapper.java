package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.data.entities.SteamGroup;
import at.emielregis.backend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SteamGroupMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SteamAccountService steamAccountService;
    private final CSGOAccountService csgoAccountService;
    private final UrlProvider urlProvider;
    private final BusyWaitingService busyWaitingService;
    private final PersistentDataService persistentDataService;

    @Value("${user-properties.account-buffer-size}")
    private long ACCOUNT_BUFFER_SIZE;

    private volatile boolean initialized = false;
    private long currentAccounts;
    private int amountOfGroups = -1;
    List<SteamGroup> groups = null;

    public SteamGroupMapper(SteamAccountService steamAccountService,
                            CSGOAccountService csgoAccountService, UrlProvider urlProvider,
                            BusyWaitingService busyWaitingService,
                            PersistentDataService persistentDataService) {
        this.steamAccountService = steamAccountService;
        this.csgoAccountService = csgoAccountService;
        this.urlProvider = urlProvider;
        this.busyWaitingService = busyWaitingService;
        this.persistentDataService = persistentDataService;
    }

    /**
     * Finds new accounts whenever the CSGOAccountMapper does not have enough accounts left to process.
     */
    public void findAccounts(RestTemplate[] restTemplates) {
        // only initialize once
        synchronized (this) {
            if (!initialized) {
                initialized = true;
                currentAccounts = steamAccountService.count();
                groups = persistentDataService.initializeGroups(getGroups());
                amountOfGroups = groups.size();
            }
        }

        // wait with other threads while one initializes the mapper
        while (!initialized) {
            Thread.onSpinWait();
        }

        LOGGER.info("Already have {} accounts", currentAccounts);

        int proxyIndex = 0;

        // whenever there are less than ACCOUNT_BUFFER_SIZE accounts left new accounts are searched for.
        while (currentAccounts < (csgoAccountService.count() + ACCOUNT_BUFFER_SIZE)) {
            String currentGroup;
            long currentPage;

            // get the group and page to process for this call
            synchronized (this) {
                currentGroup = groups.get((int) (amountOfGroups * Math.random())).getName();
                currentPage = persistentDataService.getNextPage(currentGroup);
            }

            LOGGER.info("Mapping for group {}, page {}, current accounts: {}", currentGroup, currentPage, currentAccounts);

            // get the uri
            String currentUri = urlProvider.getSteamGroupRequest(currentGroup, currentPage);

            // get the response as a string
            String response;
            try {
                response = restTemplates[proxyIndex].getForObject(currentUri, String.class);
                proxyIndex = (proxyIndex + 1) % restTemplates.length;
            } catch (Exception ex) {
                synchronized (this) { // if the call fails we free the page again as it wasn't properly mapped
                    persistentDataService.freePage(currentGroup, currentPage);
                }
                if (ex instanceof RestClientResponseException e) {
                    if (e.getRawStatusCode() == 429) {
                        LOGGER.error("429 - Too many requests");
                        busyWaitingService.wait(300);
                    }
                } else {
                    LOGGER.error(ex.getMessage());
                    busyWaitingService.wait(300);
                }
                continue;
            }

            if (response == null || response.contains("An error was encountered while processing your request")) { // shouldn't happen in practice, unless the steam servers are broken
                LOGGER.error("Error processing request for group: {}, page: {}. This means that the Steam Servers can not retrieve the members! " +
                    "Please check whether Postman returns a correct request.", currentGroup, currentPage);
                persistentDataService.freePage(currentGroup, currentPage);
                continue;
            }

            // as the request is a string all the ids are simply read using a regex matcher
            Matcher matcher = Pattern.compile("<steamID64>(?<id>\\d{17})</steamID64>").matcher(response);
            List<SteamAccount> accountList = new ArrayList<>();

            synchronized (this) {
                boolean hasAccounts = false;
                while (matcher.find()) {
                    String current = matcher.group("id");
                    hasAccounts = true;
                    if (!steamAccountService.containsById64(current)) {
                        accountList.add(SteamAccount.builder()
                            .id64(current)
                            .build()
                        );
                    }
                }

                // if the response does not contain any accounts the page number was too high for the groups user count
                if (!hasAccounts) {
                    persistentDataService.lockGroup(currentGroup);
                    groups = persistentDataService.getUnlockedGroups();
                    amountOfGroups = groups.size();
                }

                steamAccountService.saveAll(accountList);
            }

            // update the account number
            synchronized (this) {
                currentAccounts = steamAccountService.count();
            }
        }
    }

    /**
     * Returns the names of the steam groups.
     *
     * @return List of steam group names.
     */
    private List<String> getGroups() {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(CSGOAccountMapper.class.getClassLoader().getResourceAsStream("groups.txt")));
        BufferedReader reader = new BufferedReader(r);
        return reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).toList();
    }
}
