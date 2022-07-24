package at.emielregis.backend.data.responses.prices;

import at.emielregis.backend.data.dto.IPriceable;

import java.util.List;

public interface IPriceResponse {
    List<IPriceable> getPriceDtos();
}