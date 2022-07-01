package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.enums.HttpResponseMappingStatus;
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
import java.util.ArrayList;
import java.util.List;

@Component
public class CSGOInventoryMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CSGOInventoryService csgoInventoryService;
    private final ItemManager itemManager;
    private final UrlProvider urlProvider;
    private final BusyWaitingService busyWaitingService;

    public CSGOInventoryMapper(CSGOInventoryService csgoInventoryService,
                               ItemManager itemManager,
                               UrlProvider urlProvider,
                               BusyWaitingService busyWaitingService) {
        this.csgoInventoryService = csgoInventoryService;
        this.itemManager = itemManager;
        this.urlProvider = urlProvider;
        this.busyWaitingService = busyWaitingService;
    }

    public HttpResponseMappingStatus getAndSaveInventory(CSGOAccount.CSGOAccountBuilder accountBuilder, String id64, RestTemplate restTemplate) {
        LOGGER.info("Mapping inventory for user with id: {}", id64);

        HttpInventoryResponse httpInventoryResponse;
        try {
            httpInventoryResponse = restTemplate.getForObject(urlProvider.getFirstInventoryRequestUri(id64), HttpInventoryResponse.class);
        } catch (Exception ex) {
            if (ex instanceof RestClientResponseException e) {
                if (e.getRawStatusCode() == 403) {
                    return HttpResponseMappingStatus.FORBIDDEN;
                } else if (e.getRawStatusCode() == 429) {
                    busyWaitingService.wait(1);
                }
            } else {
                busyWaitingService.wait(5);
                return HttpResponseMappingStatus.NO_INTERNET;
            }
            return HttpResponseMappingStatus.FAILED;
        }

        if (httpInventoryResponse == null || !httpInventoryResponse.successful()) {
            return HttpResponseMappingStatus.FAILED;
        }

        List<TransientItem> itemList = httpInventoryResponse.getInventory();

        if (httpInventoryResponse.hasMoreItems()) {
            HttpInventoryResponse httpInventoryResponse1;
            try {
                httpInventoryResponse1 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse.getLastAssetId()), HttpInventoryResponse.class);
            } catch (RestClientResponseException e) {
                if (e.getRawStatusCode() == 429) {
                    busyWaitingService.wait(1);
                }
                return HttpResponseMappingStatus.FAILED;
            }

            if (httpInventoryResponse1 == null) {
                return HttpResponseMappingStatus.FAILED;
            }

            itemList = combineLists(itemList, httpInventoryResponse1);

            if (httpInventoryResponse1.hasMoreItems()) {
                HttpInventoryResponse httpInventoryResponse2;
                try {
                    httpInventoryResponse2 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse1.getLastAssetId()), HttpInventoryResponse.class);
                } catch (RestClientResponseException e) {
                    if (e.getRawStatusCode() == 429) {
                        busyWaitingService.wait(1);
                    }
                    return HttpResponseMappingStatus.FAILED;
                }

                if (httpInventoryResponse2 == null) {
                    return HttpResponseMappingStatus.FAILED;
                }

                itemList = combineLists(itemList, httpInventoryResponse2);
            }
        }

        CSGOInventory.CSGOInventoryBuilder builder = CSGOInventory.builder().items(itemManager.convert(itemList));
        CSGOInventory inventory = builder.build();
        csgoInventoryService.save(inventory);
        accountBuilder.csgoInventory(inventory);

        return HttpResponseMappingStatus.SUCCESS;
    }

    private List<TransientItem> combineLists(List<TransientItem> transientItems, HttpInventoryResponse httpInventoryResponse1) {
        transientItems = new ArrayList<>(transientItems);
        for (TransientItem item : httpInventoryResponse1.getInventory()) {
            TransientItem item1 = getByObject(transientItems, item);
            if (item1 != null) {
                item1.setAmount(item.getAmount() + item1.getAmount());
            } else {
                transientItems.add(item);
            }
        }
        return transientItems;
    }

    private TransientItem getByObject(List<TransientItem> transientItems, TransientItem item) {
        for (TransientItem transientItem : transientItems) {
            if (transientItem.equals(item)) {
                return transientItem;
            }
        }
        return null;
    }
}
