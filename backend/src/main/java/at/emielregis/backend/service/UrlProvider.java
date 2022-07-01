package at.emielregis.backend.service;

import org.springframework.stereotype.Component;

@Component
public class UrlProvider {
    public String getFirstInventoryRequestUri(String id64) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?l=english";
    }

    public String getInventoryRequestUriWithStart(String id64, String lastAssetId) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?l=english" + "&start_assetid=" + lastAssetId;
    }

    public String getSteamGroupRequest(String steamGroupName, long page) {
        return "https://steamcommunity.com/groups/" + steamGroupName + "/memberslistxml/?xml=1&p=" + page;
    }
}
