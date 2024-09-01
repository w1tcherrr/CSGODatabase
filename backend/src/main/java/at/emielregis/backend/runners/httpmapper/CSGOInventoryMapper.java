package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.entities.items.ItemCollection;
import at.emielregis.backend.data.enums.HttpResponseMappingStatus;
import at.emielregis.backend.data.responses.HttpInventoryResponse;
import at.emielregis.backend.service.BusyWaitingService;
import at.emielregis.backend.service.UrlProvider;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CSGOInventoryMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UrlProvider urlProvider;
    private final BusyWaitingService busyWaitingService;

    /**
     * Stores the inventory of the Steam User with the id64 in the provided accountBuilder using the
     * provided RestTemplate to send Http Calls.
     *
     * @param accountBuilder The accountBuilder in which to store the inventory.
     * @param id64           The player whose inventory to get.
     * @param restTemplate   The restTemplate.
     * @return The status of the request.
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
                    return HttpResponseMappingStatus.SUCCESS;
                } else if (e.getRawStatusCode() == 429) {
                    LOGGER.error("429 - Too many requests");
                    busyWaitingService.wait(240);
                    return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
                }
            } else { // this generally only happens if the internet is down or the proxy rejects the request
                LOGGER.error(ex.getMessage());
                return HttpResponseMappingStatus.UNKNOWN_EXCEPTION;
            }
            return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
        }

        if (httpInventoryResponse == null || !httpInventoryResponse.successful()) {
            return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
        }

        List<ItemCollection> itemList = httpInventoryResponse.getItemCollections();

        // if there are more than 500 items send a second request
        if (httpInventoryResponse.hasMoreItems()) {
            HttpInventoryResponse httpInventoryResponse1;
            try {
                httpInventoryResponse1 = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, httpInventoryResponse.getLastAssetId()), HttpInventoryResponse.class);
            } catch (Exception ex) {
                if (ex instanceof RestClientResponseException e) {
                    if (e.getRawStatusCode() == 429) {
                        LOGGER.error("429 - Too many requests");
                        busyWaitingService.wait(240);
                        return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
                    }
                } else { // this generally only happens if the internet is down or the proxy rejects the request
                    LOGGER.error(ex.getMessage());
                    return HttpResponseMappingStatus.UNKNOWN_EXCEPTION;
                }
                return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
            }

            if (httpInventoryResponse1 == null) {
                return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
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
                            busyWaitingService.wait(240);
                            return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
                        }
                    } else { // this generally only happens if the internet is down or the proxy rejects the request
                        LOGGER.error(ex.getMessage());
                        return HttpResponseMappingStatus.UNKNOWN_EXCEPTION;
                    }
                    return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
                }

                if (httpInventoryResponse2 == null) {
                    return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
                }

                itemList = combineLists(itemList, httpInventoryResponse2);
            }
        }

        // set the inventory into the builder
        CSGOInventory.CSGOInventoryBuilder builder = CSGOInventory.builder().itemCollections(itemList);
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
    private List<ItemCollection> combineLists(List<ItemCollection> transientItems, HttpInventoryResponse httpInventoryResponse1) {
        var returnList = new ArrayList<ItemCollection>();
        for (ItemCollection item : httpInventoryResponse1.getItemCollections()) {
            ItemCollection item1 = getByObject(transientItems, item);
            if (item1 != null) {
                item1.setAmount(item.getAmount() + item1.getAmount());
                returnList.add(item1);
            } else {
                returnList.add(item);
            }
        }
        return returnList;
    }

    /**
     * @param transientItems Gets the transientItem with the same classId from the list as the item provided
     * @param item           The item to be searched
     * @return The item with the same class id in the list, otherwise null.
     */
    private ItemCollection getByObject(List<ItemCollection> transientItems, ItemCollection item) {
        for (ItemCollection itemCollection : transientItems) {
            if (itemCollection.deepEquals(item)) {
                return itemCollection;
            }
        }
        return null;
    }
}
