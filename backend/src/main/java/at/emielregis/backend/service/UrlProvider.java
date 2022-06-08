package at.emielregis.backend.service;

import at.emielregis.backend.runners.SteamAccountFinder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
public class UrlProvider {
    private final List<String> API_KEYS;
    private String currentKey;
    int currentIndex = 0;

    public UrlProvider() {
        API_KEYS = getApiKeys();
        int currentIndex = new Random().nextInt(API_KEYS.size());
        currentKey = API_KEYS.get(currentIndex);
    }

    public void circleKey() {
        currentIndex = (++currentIndex) % API_KEYS.size();
        currentKey = API_KEYS.get(currentIndex);
    }

    public String getFirstInventoryRequestUri(String id64) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?key=" + currentKey + "&l=english";
    }

    public String getInventoryRequestUriWithStart(String id64, String lastAssetId) {
        return "https://steamcommunity.com/inventory/" + id64 + "/730/2?key=" + currentKey + "&l=english" + "&start_assetid=" + lastAssetId;
    }

    public String getFriendsRequestUri(String id64) {
        return "http://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=" + currentKey + "&steamid=" + id64 + "&relationship=friend";
    }

    public String getGamesRequest(String id64) {
        return "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=" + currentKey + "&format=json&steamid=" + id64;
    }

    private static List<String> getApiKeys() {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(SteamAccountFinder.class.getClassLoader().getResourceAsStream("keys.txt")));
        BufferedReader reader = new BufferedReader(r);
        return reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).toList();
    }
}
