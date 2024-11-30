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

/**
 * Maps Steam groups to retrieve and store Steam accounts.
 * This class uses the Steam API to fetch accounts from specified groups
 * and ensures that a buffer of unmapped accounts is always available for processing.
 */
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
    private List<SteamGroup> groups = null;

    /**
     * Constructs the SteamGroupMapper with required services.
     *
     * @param steamAccountService   Service for managing Steam accounts.
     * @param csgoAccountService    Service for managing CS:GO accounts.
     * @param urlProvider           Provides URIs for Steam API requests.
     * @param busyWaitingService    Handles wait logic when encountering rate limits.
     * @param persistentDataService Service for managing group and page persistence.
     */
    public SteamGroupMapper(SteamAccountService steamAccountService,
                            CSGOAccountService csgoAccountService,
                            UrlProvider urlProvider,
                            BusyWaitingService busyWaitingService,
                            PersistentDataService persistentDataService) {
        this.steamAccountService = steamAccountService;
        this.csgoAccountService = csgoAccountService;
        this.urlProvider = urlProvider;
        this.busyWaitingService = busyWaitingService;
        this.persistentDataService = persistentDataService;
    }

    /**
     * Finds and maps new Steam accounts from groups when the buffer size falls below the threshold.
     *
     * @param restTemplates Array of RestTemplate instances for making HTTP requests.
     */
    public void findAccounts(RestTemplate[] restTemplates) {
        synchronized (this) {
            if (!initialized) {
                initialized = true;
                currentAccounts = steamAccountService.count();
                groups = persistentDataService.initializeGroups(getGroups());
                amountOfGroups = groups.size();
            }
        }

        // Wait for initialization in other threads
        while (!initialized) {
            Thread.onSpinWait();
        }

        LOGGER.info("Already have {} accounts", currentAccounts);

        int proxyIndex = 0;

        // Fetch accounts until the buffer is filled
        while (currentAccounts < (csgoAccountService.count() + ACCOUNT_BUFFER_SIZE)) {
            String currentGroup;
            long currentPage;

            // Select a group and page for processing
            synchronized (this) {
                currentGroup = groups.get((int) (amountOfGroups * Math.random())).getName();
                currentPage = persistentDataService.getNextPage(currentGroup);
            }

            LOGGER.info("Mapping group: {}, page: {}, current accounts: {}", currentGroup, currentPage, currentAccounts);

            String currentUri = urlProvider.getSteamGroupRequest(currentGroup, currentPage);

            String response;
            try {
                response = restTemplates[proxyIndex].getForObject(currentUri, String.class);
                proxyIndex = (proxyIndex + 1) % restTemplates.length;
            } catch (Exception ex) {
                handleFailedRequest(ex, currentGroup, currentPage);
                continue;
            }

            if (response == null || response.contains("An error was encountered while processing your request")) {
                LOGGER.error("Error processing group: {}, page: {}. Check Steam servers.", currentGroup, currentPage);
                persistentDataService.freePage(currentGroup, currentPage);
                continue;
            }

            processResponse(response, currentGroup);
        }
    }

    /**
     * Handles failed HTTP requests and frees the page for retrying.
     *
     * @param ex          The exception encountered during the request.
     * @param currentGroup The group being processed.
     * @param currentPage The page being processed.
     */
    private void handleFailedRequest(Exception ex, String currentGroup, long currentPage) {
        synchronized (this) {
            persistentDataService.freePage(currentGroup, currentPage);
        }
        if (ex instanceof RestClientResponseException e && e.getRawStatusCode() == 429) {
            LOGGER.error("429 - Too many requests. Retrying after wait.");
            busyWaitingService.wait(240);
        } else {
            LOGGER.error("Request failed: {}", ex.getMessage());
            busyWaitingService.wait(240);
        }
    }

    /**
     * Processes the response from the Steam API, extracting and saving Steam accounts.
     *
     * @param response    The raw response from the Steam API.
     * @param currentGroup The group being processed.
     */
    private void processResponse(String response, String currentGroup) {
        Matcher matcher = Pattern.compile("<steamID64>(?<id>\\d{17})</steamID64>").matcher(response);
        List<SteamAccount> accountList = new ArrayList<>();

        synchronized (this) {
            boolean hasAccounts = false;
            while (matcher.find()) {
                String id64 = matcher.group("id");
                hasAccounts = true;
                if (!steamAccountService.containsById64(id64)) {
                    accountList.add(SteamAccount.builder().id64(id64).build());
                }
            }

            if (!hasAccounts) {
                persistentDataService.lockGroup(currentGroup);
                groups = persistentDataService.getUnlockedGroups();
                amountOfGroups = groups.size();
            }

            steamAccountService.saveAll(accountList);
            currentAccounts = steamAccountService.count();
        }
    }

    /**
     * Reads the list of group names from the `groups.txt` file in the classpath.
     *
     * @return A list of group names.
     */
    private List<String> getGroups() {
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(
            CSGOAccountMapper.class.getClassLoader().getResourceAsStream("groups.txt")));
        BufferedReader bufferedReader = new BufferedReader(reader);
        return bufferedReader.lines()
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .map(String::trim)
            .toList();
    }
}
