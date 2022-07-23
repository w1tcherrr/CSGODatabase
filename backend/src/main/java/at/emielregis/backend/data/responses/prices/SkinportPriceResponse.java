package at.emielregis.backend.data.responses.prices;

import at.emielregis.backend.data.dto.IPriceable;
import at.emielregis.backend.data.dto.PriceDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.util.List;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkinportPriceResponse implements IPriceResponse {
    @JsonProperty("market_hash_name")
    private String marketHashName;

    @JsonProperty("suggested_price")
    private Double suggestedPrice;

    @JsonProperty("mean_price")
    private Double meanPrice;

    @Override
    public List<IPriceable> getPriceDtos() {
        PriceDto.PriceDtoBuilder builder = PriceDto.builder()
            .marketHashName(marketHashName);
        if (meanPrice != null) {
            builder.suggestedPrice(meanPrice);
        } else {
            builder.suggestedPrice(suggestedPrice);
        }
        return List.of(builder.build());
    }
}
