package at.emielregis.backend.data.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpFriendsResponse {
    private List<String> friendIds;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("friendslist")
    @SuppressWarnings("unchecked")
    private void unpackNested(Map<String, Object> friendsList) {
        this.friendIds = new ArrayList<>();
        List<Map<String, Object>> friends = (List<Map<String, Object>>) friendsList.get("friends");
        friends.forEach(map -> map.forEach((key, value) -> {
            if (key.equals("steamid")) {
                friendIds.add((String) value);
            }
        }));
    }

    public List<String> getFriendId64s() {
        return friendIds;
    }
}
