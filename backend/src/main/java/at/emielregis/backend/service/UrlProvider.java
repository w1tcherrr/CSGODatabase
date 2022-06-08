package at.emielregis.backend.service;

import org.springframework.stereotype.Component;

@Component
public class UrlProvider {
    private static final String API_KEY = "BC8B482B39514C1D253946D3C6CE7696";

    public String getFirstInventoryRequestUri(String id64) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?key=" + API_KEY + "&l=english";
    }

    public String getInventoryRequestUriWithStart(String id64, String lastAssetId) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?key=" + API_KEY + "&l=english" + "&start_assetid=" + lastAssetId;
    }

    public String getFriendsRequestUri(String id64) {
        return "http://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=" + API_KEY + "&steamid=" + id64 + "&relationship=friend";
    }

    public String getGamesRequest(String id64) {
        return "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=" + API_KEY + "&format=json&steamid=" + id64;
    }
}
