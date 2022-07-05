package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.enums.HttpResponseMappingStatus;
import at.emielregis.backend.data.responses.HttpInventoryResponse;
import at.emielregis.backend.service.BusyWaitingService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.UrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to map CSGO inventories by providing the id64 of a SteamAccount.
 */
@Component
public class CSGOInventoryMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemService itemService;
    private final UrlProvider urlProvider;
    private final BusyWaitingService busyWaitingService;

    public CSGOInventoryMapper(ItemService itemService,
                               UrlProvider urlProvider,
                               BusyWaitingService busyWaitingService) {
        this.itemService = itemService;
        this.urlProvider = urlProvider;
        this.busyWaitingService = busyWaitingService;
    }

    /**
     * Stores the inventory of the Steam User with the id64 in the provided accountBuilder using the
     * provided RestTemplate to send Http Calls.
     *
     * @param accountBuilder The accountBuilder in which to store the inventory.
     * @param id64           The player whose inventory to get.
     * @param restTemplate   The restTemplate.
     * @return FORBIDDEN if the Account is private, UNKNOWN_EXCEPTION if the Rest Call fails for unknown reasons,
     * FAILED if the API returns a 429 status code or SUCCESS if everything works correctly.
     */
    public HttpResponseMappingStatus getInventory(CSGOAccount.CSGOAccountBuilder accountBuilder, String id64, RestTemplate restTemplate) {
        LOGGER.info("Mapping inventory for user with id: {}", id64);

        // get the first 500 items of the inventory
        HttpInventoryResponse httpInventoryResponse;
        try {
            httpInventoryResponse = restTemplate.getForObject(urlProvider.getFirstInventoryRequestUri(id64), HttpInventoryResponse.class);
        } catch (Exception ex) {
            if (ex instanceof RestClientResponseException e) {
                if (e.getRawStatusCode() == 403) { // this is returned when the inventory (or the account) of the user is private
                    return HttpResponseMappingStatus.FORBIDDEN;
                } else if (e.getRawStatusCode() == 429) {
                    LOGGER.error("429 - Too many requests");
                    busyWaitingService.wait(1);
                }
            } else { // this generally only happens if the internet is down or the proxy rejects the request
                LOGGER.error(ex.getMessage());
                busyWaitingService.wait(5);
                return HttpResponseMappingStatus.UNKNOWN_EXCEPTION;
            }
            return HttpResponseMappingStatus.FAILED;
        }

        if (httpInventoryResponse == null || !httpInventoryResponse.successful()) {
            return HttpResponseMappingStatus.FAILED;
        }

        List<TransientItem> itemList = httpInventoryResponse.getTransientItems();

        // if there are more than 500 items send a second request
        if (httpInventoryResponse.hasMoreItems()) {
            HttpInventoryResponse httpInventoryResponse1;
            try {
                httpInventoryResponse1 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse.getLastAssetId()), HttpInventoryResponse.class);
            } catch (Exception ex) {
                if (ex instanceof RestClientResponseException e) {
                    if (e.getRawStatusCode() == 429) {
                        LOGGER.error("429 - Too many requests");
                        busyWaitingService.wait(1);
                    }
                } else { // this generally only happens if the internet is down or the proxy rejects the request
                    LOGGER.error(ex.getMessage());
                    busyWaitingService.wait(5);
                    return HttpResponseMappingStatus.UNKNOWN_EXCEPTION;
                }
                return HttpResponseMappingStatus.FAILED;
            }

            if (httpInventoryResponse1 == null) {
                return HttpResponseMappingStatus.FAILED;
            }

            itemList = combineLists(itemList, httpInventoryResponse1);

            // if there are more than 1000 items (this is extremely rare, since the limit is theoretically 1000 but can be exceeded by a few items)
            if (httpInventoryResponse1.hasMoreItems()) {
                HttpInventoryResponse httpInventoryResponse2;
                try {
                    httpInventoryResponse2 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse1.getLastAssetId()), HttpInventoryResponse.class);
                } catch (Exception ex) {
                    if (ex instanceof RestClientResponseException e) {
                        if (e.getRawStatusCode() == 429) {
                            LOGGER.error("429 - Too many requests");
                            busyWaitingService.wait(1);
                        }
                    } else { // this generally only happens if the internet is down or the proxy rejects the request
                        LOGGER.error(ex.getMessage());
                        busyWaitingService.wait(5);
                        return HttpResponseMappingStatus.UNKNOWN_EXCEPTION;
                    }
                    return HttpResponseMappingStatus.FAILED;
                }

                if (httpInventoryResponse2 == null) {
                    return HttpResponseMappingStatus.FAILED;
                }

                itemList = combineLists(itemList, httpInventoryResponse2);
            }
        }

        // set the inventory into the builder
        CSGOInventory.CSGOInventoryBuilder builder = CSGOInventory.builder().items(
            // convert the transient items into normal items
            itemService.convert(itemList)
        );
        CSGOInventory inventory = builder.build();
        accountBuilder.csgoInventory(inventory);

        return HttpResponseMappingStatus.SUCCESS;
    }

    /**
     * Combines two lists of transient items into one list by combining equal items into one.
     *
     * @param transientItems         first list of items
     * @param httpInventoryResponse1 the response containing the second list of items
     * @return The combined list
     */
    private List<TransientItem> combineLists(List<TransientItem> transientItems, HttpInventoryResponse httpInventoryResponse1) {
        transientItems = new ArrayList<>(transientItems);
        for (TransientItem item : httpInventoryResponse1.getTransientItems()) {
            TransientItem item1 = getByObject(transientItems, item);
            if (item1 != null) {
                item1.setAmount(item.getAmount() + item1.getAmount());
            } else {
                transientItems.add(item);
            }
        }
        return transientItems;
    }

    /**
     * @param transientItems Gets the transientItem with the same classId from the list as the item provided
     * @param item           The item to be searched
     * @return The item with the same class id in the list, otherwise null.
     */
    private TransientItem getByObject(List<TransientItem> transientItems, TransientItem item) {
        for (TransientItem transientItem : transientItems) {
            if (transientItem.equals(item)) {
                return transientItem;
            }
        }
        return null;
    }
}
