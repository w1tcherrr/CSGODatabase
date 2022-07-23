package at.emielregis.backend.data.responses.prices;

import at.emielregis.backend.data.dto.IPriceable;
import at.emielregis.backend.data.dto.PriceDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamMarketPriceResponse implements IPriceResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("total_count")
    private Integer totalItems;

    @JsonProperty("start")
    private int start;

    private final List<IPriceable> priceDtos = new ArrayList<>();

    @JsonProperty("results")
    private void unpackResults(List<Map<String, Object>> results) {
        results.forEach(result -> {
            PriceDto.PriceDtoBuilder priceDtoBuilder = PriceDto.builder();
            result.forEach((k, v) -> {
                switch (k) {
                    case "hash_name" -> priceDtoBuilder.marketHashName((String) v);
                    case "sell_price" -> {
                        int price1 = (int) v;
                        double price = ((double) price1) / 100;
                        priceDtoBuilder.suggestedPrice(price);
                    }
                }
            });
            priceDtos.add(priceDtoBuilder.build());
        });
    }

    @Override
    public List<IPriceable> getPriceDtos() {
        return priceDtos;
    }

    public boolean isSuccessful() {
        return success;
    }

    public boolean shouldHaveItems() {
        return totalItems == null || totalItems == 0 || start < totalItems;
    }

    public boolean hasNoItems() {
        return priceDtos.size() == 0;
    }
}
