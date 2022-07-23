package at.emielregis.backend.runners.httpmapper;

import at.emielregis.backend.data.dto.IPriceable;
import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemType;
import at.emielregis.backend.data.responses.prices.CsgoBackpackPriceResponse;
import at.emielregis.backend.data.responses.prices.IPriceResponse;
import at.emielregis.backend.data.responses.prices.SkinportPriceResponse;
import at.emielregis.backend.data.responses.prices.SteamMarketPriceResponse;
import at.emielregis.backend.service.BusyWaitingService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.ItemTypeService;
import at.emielregis.backend.service.ProxyService;
import at.emielregis.backend.service.UrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ItemPriceMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RestTemplate restTemplate;
    private final UrlProvider urlProvider;
    private final BusyWaitingService busyWaitingService;
    private final ProxyService proxyService;
    private final ItemTypeService itemTypeService;
    private final ItemService itemService;

    private final Map<String, List<IPriceable>> priceDtos = new HashMap<>();

    public ItemPriceMapper(RestTemplate restTemplate, UrlProvider urlProvider, BusyWaitingService busyWaitingService, ProxyService proxyService, ItemTypeService itemTypeService, ItemService itemService) {
        this.restTemplate = restTemplate;
        this.urlProvider = urlProvider;
        this.busyWaitingService = busyWaitingService;
        this.proxyService = proxyService;
        this.itemTypeService = itemTypeService;
        this.itemService = itemService;
    }

    public void start() {
        LOGGER.info("RUNNING ITEM PRICE MAPPER");
        parseSkinPortPrices();
        parseCsgoBackPackPrices();
        priceDtos.values().forEach(list -> {
            if (list.size() != 2) {
                System.out.println(list + "\n\n");
            }
        });
    }

    private void parseSkinPortPrices() {
        LOGGER.info("RUNNING SKINPORT PRICE MAPPER");
        SkinportPriceResponse[] skinportPriceResponses = restTemplate.getForObject(urlProvider.getSkinPortPriceUrl(), SkinportPriceResponse[].class);
        for (SkinportPriceResponse response : Objects.requireNonNull(skinportPriceResponses)) {
            addDtos(response);
        }
    }

    private void parseCsgoBackPackPrices() {
        LOGGER.info("RUNNING CSGO-BACKPACK PRICE MAPPER");
        CsgoBackpackPriceResponse csgoBackpackPriceResponse = restTemplate.getForObject(urlProvider.getCsgoBackPackUrl(), CsgoBackpackPriceResponse.class);
        if (Objects.requireNonNull(csgoBackpackPriceResponse).isSuccessful()) {
            addDtos(csgoBackpackPriceResponse);
        } else {
            throw new IllegalStateException("CSGO-BACKPACK RESPONSE FAILED.");
        }
    }

    // the api is really slow, so it doesn't work well, even with 100 proxies
    private void parseSteamMarketPrices() {
        LOGGER.info("RUNNING STEAM MARKET PRICE MAPPER");
        int THREAD_AMOUNT = 100;

        List<Integer> pagesToMap = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i <= 25000; i += 100) {
            pagesToMap.add(i);
        }

        List<List<Integer>> distributedIds = IntStream.range(0, THREAD_AMOUNT).mapToObj(i -> new ArrayList<Integer>()).map(obj -> (List<Integer>) obj).collect(Collectors.toList());

        for (int j = 0; j < pagesToMap.size(); j++) {
            int curr = j % THREAD_AMOUNT;
            List<Integer> currentList = distributedIds.get(curr);
            currentList.add(pagesToMap.get(j));
            distributedIds.set(curr, currentList);
        }

        List<Integer> successful = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger currentThreadId = new AtomicInteger(0);

        proxyService.addThreads(THREAD_AMOUNT, template -> {
            List<Integer> idsForThread = distributedIds.get(currentThreadId.getAndIncrement());

            while (true) {
                SteamMarketPriceResponse steamMarketPriceResponse;
                Integer currentPage = null;
                try {
                    if (idsForThread.size() > 0) {
                        currentPage = idsForThread.get(0);
                        idsForThread = idsForThread.subList(1, idsForThread.size());
                    } else {
                        LOGGER.info("NO PAGES LEFT TO MAP - FINISHED MAPPING");
                        break;
                    }
                    LOGGER.info("MAPPING FOR ID: {}", currentPage);
                    steamMarketPriceResponse = template.getForObject(urlProvider.getSteamMarketPriceUrl(currentPage), SteamMarketPriceResponse.class);
                } catch (RestClientException e) {
                    LOGGER.error(e.getMessage());
                    idsForThread.add(currentPage);
                    busyWaitingService.wait(5);
                    continue;
                }

                if (Objects.requireNonNull(steamMarketPriceResponse).isSuccessful()) {
                    LOGGER.info("SUCCESSFUL RESPONSE");
                    if (steamMarketPriceResponse.hasNoItems()) {
                        if (steamMarketPriceResponse.shouldHaveItems()) {
                            LOGGER.error("RESPONSE FOR ID: {} HAS NO ITEMS BUT SHOULD HAVE ITEMS", currentPage);
                            idsForThread.add(currentPage);
                            busyWaitingService.wait(5);
                            continue;
                        } else {
                            LOGGER.info("RESPONSE FOR ID: {} HAS NO ITEMS - FINISHED MAPPING", currentPage);
                            break;
                        }
                    }
                    successful.add(currentPage);
                    addDtos(steamMarketPriceResponse);
                } else {
                    LOGGER.error("UNSUCCESSFUL RESPONSE!");
                    idsForThread.add(currentPage);
                    busyWaitingService.wait(5);
                }
            }
        });

        proxyService.await();

        System.out.println(successful.stream().sorted(Comparator.comparingInt(v -> v)).collect(Collectors.toList()));
    }

    private synchronized void addDtos(IPriceResponse response) {
        Objects.requireNonNull(response).getPriceDtos().forEach(priceDto -> {
            if (priceDto.getSuggestedPrice() == null) {
                return;
            }
            String marketHashName = priceDto.getMarketHashName();
            if (priceDtos.get(marketHashName) != null) {
                List<IPriceable> current = priceDtos.get(marketHashName);
                current.add(priceDto);
                priceDtos.put(marketHashName, current);
            } else {
                priceDtos.put(marketHashName, new ArrayList<>(List.of(priceDto)));
            }
        });
    }

    public Double getTotalPriceForName(ItemName itemName, long amountOfItems) {
        List<ItemType> types = itemTypeService.getTypesForItemNames(List.of(itemName));

        double totalPrice = 0.0;

        long itemsWithPrice = 0;

        for (ItemType type : types) {
            List<IPriceable> prices = priceDtos.get(type.getMarketHashName());
            if (prices == null || prices.size() == 0) {
                continue;
            }
            int amount = itemService.getTotalAmountForType(type);
            totalPrice += amount * averagePrice(prices);
            itemsWithPrice += amount;
        }

        long leftOverItems = amountOfItems - itemsWithPrice;

        // in this case less than half of the items of the type can be priced - this is too imprecise
        if (leftOverItems > itemsWithPrice) {
            return null;
        } else {
            totalPrice *= (1 + (((double) leftOverItems) / amountOfItems));
        }

        return totalPrice;
    }

    private double averagePrice(List<IPriceable> prices) {
        double averagePrice = 0;

        for (IPriceable price : prices) {
            averagePrice += price.getSuggestedPrice();
        }

        return averagePrice / prices.size();
    }
}
