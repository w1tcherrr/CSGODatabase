package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.service.BusyWaitingService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SteamGroupMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SteamAccountService steamAccountService;
    private final UrlProvider urlProvider;
    private final RestTemplate restTemplate;
    private final BusyWaitingService busyWaitingService;

    private boolean initialized = false;
    private long currentAccounts;
    private long currentPage;

    public SteamGroupMapper(SteamAccountService steamAccountService,
                            UrlProvider urlProvider,
                            RestTemplate restTemplate,
                            BusyWaitingService busyWaitingService) {
        this.steamAccountService = steamAccountService;
        this.urlProvider = urlProvider;
        this.restTemplate = restTemplate;
        this.busyWaitingService = busyWaitingService;
    }

    public void findAccounts(long amount) {
        LOGGER.info("Finding {} accounts", amount);

        synchronized (this) {
            if (!initialized) {
                initialized = true;
                currentAccounts = steamAccountService.count();
                currentPage = currentAccounts / 1000;
            }
        }

        LOGGER.info("Already have {} accounts", currentAccounts);

        while (currentAccounts < amount) {
            long page;

            synchronized (this) {
                ++currentPage;
                page = currentPage;
            }

            LOGGER.info("Mapping for page {}, current accounts: {}", page, currentAccounts);

            String currentUri = urlProvider.getSteamGroupRequest("hentaii", page);

            String response;
            try {
                response = restTemplate.getForObject(currentUri, String.class);
            } catch (Exception ex) {
                if (ex instanceof RestClientResponseException e) {
                    if (e.getRawStatusCode() == 429) {
                        synchronized (this) {
                            --currentPage;
                        }
                        busyWaitingService.wait(3);
                    }
                } else {
                    synchronized (this) {
                        --currentPage;
                    }
                    busyWaitingService.wait(5);
                }
                continue;
            }

            if (response == null) {
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
                currentAccounts = steamAccountService.count();
            }
        }
    }
}
