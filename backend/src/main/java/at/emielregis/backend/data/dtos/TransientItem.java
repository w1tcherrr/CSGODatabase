package at.emielregis.backend.data.dtos;

import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.Rarity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransientItem {
    private String classID;
    private int amount = 0;
    private String name;
    private String nameTag;
    private boolean tradable;
    private boolean statTrak;
    private boolean souvenir;
    private TransientItemCategory category;
    private List<TransientSticker> stickers;
    private Exterior exterior;
    private Rarity rarity;
    private TransientItemSet itemSet;

    public TransientItem increaseAmount() {
        ++amount;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransientItem that = (TransientItem) o;
        return Objects.equals(classID, that.classID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classID);
    }
}
