package at.emielregis.backend.data.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class HttpGameResponse {
    private boolean privateGames;
    private boolean hasCsgo;

    @JsonProperty("success")
    private Integer success;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("response")
    @SuppressWarnings("unchecked")
    private void unpackNested(Map<String, Object> response) {
        hasCsgo = false;
        List<Map<String, Object>> games = (List<Map<String, Object>>) response.get("games");
        if (games == null || games.size() == 0) {
            privateGames = true;
            return;
        }
        games.forEach(map -> map.forEach((key, value) -> {
            if (key.equals("appid") && value.equals(730)) {
                hasCsgo = true;
            }
        }));
    }

    public boolean hasCsgo() {
        return hasCsgo;
    }

    public boolean successful() {
        return success != null && success == 1;
    }


    public boolean hasGamesPrivate() {
        return privateGames;
    }
}
