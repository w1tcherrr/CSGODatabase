package at.emielregis.backend.data.responses.prices;

import at.emielregis.backend.data.dto.IPriceable;
import at.emielregis.backend.data.dto.PriceDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CsgoBackpackPriceResponse implements IPriceResponse {

    private static final int MIN_ITEMS_SOLD_PER_ITEM = 3;

    @JsonProperty("success")
    private boolean success;

    private final List<IPriceable> priceDtos = new ArrayList<>();

    @JsonProperty("items_list")
    private void unpackItemsList(Map<String, Object> itemMap) {
        itemMap.forEach((k, v) -> {
            PriceDto.PriceDtoBuilder builder = PriceDto.builder();
            AtomicBoolean currentSuccess = new AtomicBoolean(true);

            Map<String, Object> innerMap = (Map<String, Object>) v;
            innerMap.forEach((k1, v1) -> {
                currentSuccess.set(true);
                switch (k1) {
                    // some characters are encoded like in a url...
                    case "name" -> {
                        String name = (String) v1;
                        // this api returns some items with two different encodings, this filters duplicates...
                        if (name.contains("%27")) {
                            currentSuccess.set(false);
                            return;
                        }
                        name = name.replace("&#39", "'");

                        builder.marketHashName(name);
                    }
                    case "price" -> {
                        Map<String, Object> priceMap = (Map<String, Object>) v1;
                        AtomicBoolean finished = new AtomicBoolean(false);
                        priceMap.forEach((k2, v2) -> {
                            if (finished.get()) {
                                return;
                            }
                            Map<String, Object> innerPriceMap = (Map<String, Object>) v2;
                            int amountSold;
                            String stringAmount = (String) innerPriceMap.get("sold");
                            if (StringUtils.isEmpty(stringAmount)) {
                                amountSold = 0;
                            } else {
                                amountSold = Integer.parseInt(stringAmount);
                            }
                            if (amountSold > MIN_ITEMS_SOLD_PER_ITEM) {
                                finished.set(true);
                                builder.suggestedPrice(parseNumber(innerPriceMap.get("median")));
                            }
                        });
                    }
                }
            });

            if (currentSuccess.get()) {
                priceDtos.add(builder.build());
            }
        });
    }

    private Double parseNumber(Object num) {
        if (num instanceof Integer) {
            return (double) (int) num;
        }
        if (num instanceof Double) {
            return (double) num;
        }
        throw new IllegalStateException("Invalid number " + num);
    }

    @Override
    public List<IPriceable> getPriceDtos() {
        return priceDtos;
    }

    public boolean isSuccessful() {
        return success;
    }
}
