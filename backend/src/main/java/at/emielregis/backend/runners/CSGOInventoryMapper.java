package at.emielregis.backend.runners;

import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.data.responses.HttpInventoryResponse;
import at.emielregis.backend.service.BusyWaitingService;
import at.emielregis.backend.service.CSGOInventoryService;
import at.emielregis.backend.service.ItemManager;
import at.emielregis.backend.service.UrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.Map;

@Component
public class CSGOInventoryMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CSGOInventoryService csgoInventoryService;
    private final ItemManager itemManager;
    private final RestTemplate restTemplate;
    private final UrlProvider urlProvider;
    private final BusyWaitingService busyWaitingService;

    public CSGOInventoryMapper(CSGOInventoryService csgoInventoryService,
                               ItemManager itemManager,
                               UrlProvider urlProvider,
                               BusyWaitingService busyWaitingService,
                               RestTemplate restTemplate) {
        this.csgoInventoryService = csgoInventoryService;
        this.itemManager = itemManager;
        this.restTemplate = restTemplate;
        this.urlProvider = urlProvider;
        this.busyWaitingService = busyWaitingService;
    }

    public boolean getInventory(SteamAccount.SteamAccountBuilder accountBuilder, String id64) {
        LOGGER.info("Mapping inventory for user with id: {}", id64);

        HttpInventoryResponse httpInventoryResponse;
        try {
            httpInventoryResponse = restTemplate.getForObject(urlProvider.getFirstInventoryRequestUri(id64), HttpInventoryResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 403) {
                accountBuilder.withCSGOInventory(null);
                return true;
            } else if (e.getRawStatusCode() == 429) {
                busyWaitingService.waitAndCircleKey(2);
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
                httpInventoryResponse1 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse.getLastAssetId()), HttpInventoryResponse.class);
            } catch (RestClientResponseException e) {
                if (e.getRawStatusCode() == 429) {
                    busyWaitingService.waitAndCircleKey(2);
                }
                return false;
            }

            if (httpInventoryResponse1 == null) {
                return false;
            }

            combineMaps(inventoryMap, typeMap, httpInventoryResponse1);

            if (httpInventoryResponse1.hasMoreItems()) {
                HttpInventoryResponse httpInventoryResponse2;
                try {
                    httpInventoryResponse2 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse1.getLastAssetId()), HttpInventoryResponse.class);
                } catch (RestClientResponseException e) {
                    if (e.getRawStatusCode() == 429) {
                        busyWaitingService.waitAndCircleKey(2);
                    }
                    return false;
                }

                if (httpInventoryResponse2 == null) {
                    return false;
                }

                combineMaps(inventoryMap, typeMap, httpInventoryResponse2);
            }
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
}
