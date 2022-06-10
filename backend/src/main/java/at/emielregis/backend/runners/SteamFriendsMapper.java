package at.emielregis.backend.runners;

import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.data.responses.HttpFriendsResponse;
import at.emielregis.backend.service.BusyWaitingService;
import at.emielregis.backend.service.UrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;

@Component
public class SteamFriendsMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RestTemplate restTemplate;
    private final BusyWaitingService busyWaitingService;
    private final UrlProvider urlProvider;

    public SteamFriendsMapper(RestTemplate restTemplate,
                              BusyWaitingService busyWaitingService,
                              UrlProvider urlProvider) {
        this.restTemplate = restTemplate;
        this.urlProvider = urlProvider;
        this.busyWaitingService = busyWaitingService;
    }

    public boolean mapFriends(SteamAccount.SteamAccountBuilder accountBuilder, String id64) {
        LOGGER.info("Mapping friends for user with id: {}", id64);

        HttpFriendsResponse httpFriendsResponse;
        try {
            httpFriendsResponse = restTemplate.getForObject(urlProvider.getFriendsRequestUri(id64), HttpFriendsResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 429) {
                busyWaitingService.waitAndCircleKey(2);
            }
            return false;
        }

        if (httpFriendsResponse != null) {
            accountBuilder.withFriendIds(httpFriendsResponse.getFriendId64s());
        } else {
            accountBuilder.withFriendIds(null);
        }

        return true;
    }
}
