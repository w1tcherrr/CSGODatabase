package at.emielregis.backend.service;

import org.springframework.stereotype.Component;

@Component
public class UrlProvider {
    /**
     * Gets the first inventory request uri for a user.
     *
     * @param id64 The id64 of the user.
     * @return The url.
     */
    public String getFirstInventoryRequestUri(String id64) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?l=english";
    }

    /**
     * Gets the inventory request uri for a user with the specified lastAssetId returned from the last request.
     *
     * @param id64 The id64 of the user.
     * @return The url.
     */
    public String getInventoryRequestUriWithStart(String id64, String lastAssetId) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?l=english" + "&start_assetid=" + lastAssetId;
    }

    /**
     * Gets the steam group xml request page for the specified group and page.
     *
     * @param steamGroupName The name of the steam group.
     * @param page The page number to be retrieved.
     * @return The url.
     */
    public String getSteamGroupRequest(String steamGroupName, long page) {
        return "https://steamcommunity.com/groups/" + steamGroupName + "/memberslistxml/?xml=1&p=" + page;
    }
}
