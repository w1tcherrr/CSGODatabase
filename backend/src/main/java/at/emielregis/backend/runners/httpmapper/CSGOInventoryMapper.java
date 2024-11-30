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
 * Responsible for mapping CS:GO inventories for Steam accounts.
 * Fetches and combines inventory items from the Steam API.
 */
@Component
@RequiredArgsConstructor
public class CSGOInventoryMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UrlProvider urlProvider;
    private final BusyWaitingService busyWaitingService;

    /**
     * Fetches and maps the CS:GO inventory for a given Steam account.
     * Uses a RestTemplate for HTTP requests to the Steam API.
     *
     * @param accountBuilder The account builder for which the inventory is being fetched.
     * @param id64           The SteamID64 of the account.
     * @param restTemplate   The RestTemplate instance for HTTP calls.
     * @return The status of the inventory fetch request.
     */
    public HttpResponseMappingStatus getInventory(CSGOAccount.CSGOAccountBuilder accountBuilder, String id64, RestTemplate restTemplate) {
        LOGGER.info("Fetching inventory for user with ID: {}", id64);

        HttpInventoryResponse initialResponse;

        try {
            initialResponse = restTemplate.getForObject(urlProvider.getFirstInventoryRequestUri(id64), HttpInventoryResponse.class);
        } catch (Exception ex) {
            return handleException(ex);
        }

        if (initialResponse == null || !initialResponse.successful()) {
            return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
        }

        List<ItemCollection> itemList = initialResponse.getItemCollections();

        // Fetch additional pages if inventory has more items
        if (initialResponse.hasMoreItems()) {
            itemList = fetchAdditionalPages(itemList, id64, initialResponse, restTemplate);
        }

        // Assign inventory to the account builder
        CSGOInventory inventory = CSGOInventory.builder().itemCollections(itemList).build();
        accountBuilder.csgoInventory(inventory);

        return HttpResponseMappingStatus.SUCCESS;
    }

    /**
     * Handles exceptions during HTTP requests.
     *
     * @param ex The exception to be handled.
     * @return The corresponding {@link HttpResponseMappingStatus}.
     */
    private HttpResponseMappingStatus handleException(Exception ex) {
        if (ex instanceof RestClientResponseException e) {
            if (e.getRawStatusCode() == 403) {
                // Inventory or account is private
                return HttpResponseMappingStatus.SUCCESS;
            } else if (e.getRawStatusCode() == 429) {
                LOGGER.error("429 - Too many requests. Retrying after wait.");
                busyWaitingService.wait(240);
                return HttpResponseMappingStatus.TOO_MANY_REQUESTS;
            } else if (e.getRawStatusCode() == 401) {
                LOGGER.error("401 - Unauthorized. Proxy may not have access.");
                busyWaitingService.wait(60);
                return HttpResponseMappingStatus.UNKNOWN_EXCEPTION;
            }
        } else {
            LOGGER.error("Exception during HTTP request: {}", ex.getMessage());
        }
        return HttpResponseMappingStatus.UNKNOWN_EXCEPTION;
    }

    /**
     * Fetches additional inventory pages if the inventory exceeds the item limit for a single response.
     *
     * @param itemList        The initial list of items from the first page.
     * @param id64            The SteamID64 of the account.
     * @param previousResponse The response containing the lastAssetId for pagination.
     * @param restTemplate    The RestTemplate for HTTP calls.
     * @return The complete list of items from all pages.
     */
    private List<ItemCollection> fetchAdditionalPages(List<ItemCollection> itemList, String id64, HttpInventoryResponse previousResponse, RestTemplate restTemplate) {
        HttpInventoryResponse nextPageResponse;
        String lastAssetId = previousResponse.getLastAssetId();

        try {
            nextPageResponse = restTemplate.getForObject(urlProvider.getInventoryRequestUriWithStart(id64, lastAssetId), HttpInventoryResponse.class);
        } catch (Exception ex) {
            handleException(ex);
            return itemList;
        }

        if (nextPageResponse != null) {
            itemList = combineLists(itemList, nextPageResponse);

            // If there are even more items, fetch additional pages
            if (nextPageResponse.hasMoreItems()) {
                return fetchAdditionalPages(itemList, id64, nextPageResponse, restTemplate);
            }
        }

        return itemList;
    }

    /**
     * Combines items from multiple inventory pages into a single list.
     *
     * @param existingItems The list of items from previous pages.
     * @param newResponse   The response containing items from the next page.
     * @return A combined list of items.
     */
    private List<ItemCollection> combineLists(List<ItemCollection> existingItems, HttpInventoryResponse newResponse) {
        List<ItemCollection> combinedList = new ArrayList<>(existingItems);

        for (ItemCollection newItem : newResponse.getItemCollections()) {
            ItemCollection existingItem = findMatchingItem(existingItems, newItem);
            if (existingItem != null) {
                existingItem.setAmount(existingItem.getAmount() + newItem.getAmount());
            } else {
                combinedList.add(newItem);
            }
        }

        return combinedList;
    }

    /**
     * Finds a matching item in the existing list based on deep equality.
     *
     * @param items The list of existing items.
     * @param targetItem The item to find a match for.
     * @return The matching item if found, otherwise null.
     */
    private ItemCollection findMatchingItem(List<ItemCollection> items, ItemCollection targetItem) {
        return items.stream().filter(item -> item.deepEquals(targetItem)).findFirst().orElse(null);
    }
}
