package at.emielregis.backend.service;

import org.springframework.stereotype.Component;

/**
 * Service for providing URLs for various Steam and external API requests.
 */
@Component
public class UrlProvider {

    /**
     * Constructs the first inventory request URL for a user.
     *
     * @param id64 The Steam ID64 of the user.
     * @return The constructed URL for the inventory request.
     */
    public String getFirstInventoryRequestUri(String id64) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?l=english";
    }

    /**
     * Constructs the inventory request URL for a user with a specific starting asset ID.
     *
     * @param id64        The Steam ID64 of the user.
     * @param lastAssetId The last asset ID from the previous request.
     * @return The constructed URL for the inventory request.
     */
    public String getInventoryRequestUriWithStart(String id64, String lastAssetId) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?l=english&start_assetid=" + lastAssetId;
    }

    /**
     * Constructs the request URL for fetching a specific page of a Steam group members list in XML format.
     *
     * @param steamGroupName The name of the Steam group.
     * @param page           The page number to be fetched.
     * @return The constructed URL for the Steam group request.
     */
    public String getSteamGroupRequest(String steamGroupName, long page) {
        return "https://steamcommunity.com/groups/" + steamGroupName + "/memberslistxml/?xml=1&p=" + page;
    }

    /**
     * Provides the URL for fetching SkinPort prices for CS:GO items.
     *
     * @return The constructed URL for SkinPort prices API.
     */
    public String getSkinPortPriceUrl() {
        return "https://api.skinport.com/v1/items?app_id=730&currency=USD";
    }

    /**
     * Constructs the URL for fetching Steam Market prices starting from a specific offset.
     *
     * @param start The offset for the market search results.
     * @return The constructed URL for the Steam Market prices API.
     */
    public String getSteamMarketPriceUrl(long start) {
        return "https://steamcommunity.com/market/search/render/?search_descriptions=0&sort_column=default&sort_dir=desc&appid=730&norender=1&count=100&start=" + start;
    }

    /**
     * Provides the URL for fetching item prices from the CS:GO Backpack API.
     *
     * @return The constructed URL for the CS:GO Backpack API.
     */
    public String getCsgoBackPackUrl() {
        return "http://csgobackpack.net/api/GetItemsList/v2/?no_details=true";
    }
}
