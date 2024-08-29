package at.emielregis.backend.data.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PriceDto implements IPriceable {
    private String marketHashName;
    private Double suggestedPrice;
}
