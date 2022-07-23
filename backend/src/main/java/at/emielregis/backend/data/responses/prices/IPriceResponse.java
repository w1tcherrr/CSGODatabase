package at.emielregis.backend.data.responses.prices;

import at.emielregis.backend.data.dto.IPriceable;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface IPriceResponse {
    List<IPriceable> getPriceDtos();
}