package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.service.BusyWaitingService;
import at.emielregis.backend.service.PersistentDataService;
import at.emielregis.backend.service.SteamAccountService;
import at.emielregis.backend.service.UrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SteamGroupMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SteamAccountService steamAccountService;
    private final UrlProvider urlProvider;
    private final RestTemplate restTemplate;
    private final BusyWaitingService busyWaitingService;
    private final PersistentDataService persistentDataService;

    private volatile boolean initialized = false;
    private long currentAccounts;
    private int amountOfGroups = -1;
    Map<String, Integer> groupMap = null;

    public SteamGroupMapper(SteamAccountService steamAccountService,
                            UrlProvider urlProvider,
                            RestTemplate restTemplate,
                            BusyWaitingService busyWaitingService,
                            PersistentDataService persistentDataService) {
        this.steamAccountService = steamAccountService;
        this.urlProvider = urlProvider;
        this.restTemplate = restTemplate;
        this.busyWaitingService = busyWaitingService;
        this.persistentDataService = persistentDataService;
    }

    public void findAccounts(List<String> steamGroups, long amount) {
        LOGGER.info("Finding {} accounts", amount);

        synchronized (this) {
            if (!initialized) {
                initialized = true;
                currentAccounts = steamAccountService.count();
                groupMap = persistentDataService.initializeGroups(steamGroups);
                amountOfGroups = groupMap.keySet().size();
            }
        }

        while (!initialized) {
            Thread.onSpinWait();
        }

        LOGGER.info("Already have {} accounts", currentAccounts);

        while (currentAccounts < amount) {
            String currentGroup;
            long currentPage;

            synchronized (this) {
                currentGroup = groupMap.keySet().stream().toList().get((int) (amountOfGroups * Math.random()));
                currentPage = groupMap.get(currentGroup);
                groupMap.put(currentGroup, groupMap.get(currentGroup) + 1);
            }

            LOGGER.info("Mapping for group {}, page {}, current accounts: {}", currentGroup, currentPage, currentAccounts);

            String currentUri = urlProvider.getSteamGroupRequest(currentGroup, currentPage);

            String response;
            try {
                response = restTemplate.getForObject(currentUri, String.class);
            } catch (Exception ex) {
                if (ex instanceof RestClientResponseException e) {
                    if (e.getRawStatusCode() == 429) {
                        synchronized (this) {
                            groupMap.put(currentGroup, groupMap.get(currentGroup) - 1);
                        }
                        LOGGER.error("429 - Too many requests");
                        busyWaitingService.wait(3);
                    }
                } else {
                    synchronized (this) {
                        groupMap.put(currentGroup, groupMap.get(currentGroup) - 1);
                    }
                    LOGGER.error(ex.getMessage());
                    busyWaitingService.wait(5);
                }
                continue;
            }

            if (response == null) { // should never happen in practice
                groupMap.put(currentGroup, groupMap.get(currentGroup) - 1);
                continue;
            }

            Matcher matcher = Pattern.compile("<steamID64>(?<id>\\d{17})</steamID64>").matcher(response);
            List<SteamAccount> accountList = new ArrayList<>();

            synchronized (this) {
                while (matcher.find()) {
                    String current = matcher.group("id");
                    if (!steamAccountService.containsById64(current)) {
                        accountList.add(SteamAccount.builder()
                            .id64(current)
                            .build()
                        );
                    }
                }
                steamAccountService.saveAll(accountList);
            }

            synchronized (this) {
                persistentDataService.updateGroups(groupMap);
                currentAccounts = steamAccountService.count();
            }
        }
    }
}
