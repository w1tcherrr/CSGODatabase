package at.emielregis.backend.data.entities.items;

import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.Rarity;
import at.emielregis.backend.data.enums.SpecialItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ItemName itemName;

    @Column(updatable = false)
    private Exterior exterior;

    @Column(updatable = false)
    private Rarity rarity;

    @ManyToOne(optional = false)
    private ItemCategory category;

    @ManyToOne
    private ItemSet itemSet;

    @Column(nullable = false, updatable = false)
    private SpecialItemType specialItemType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ItemType itemType = (ItemType) o;
        return id != null && Objects.equals(id, itemType.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean deepEquals(ItemType itemType) {
        return this.exterior == itemType.exterior &&
            this.itemName.deepEquals(itemType.getItemName()) &&
            this.category.deepEquals(itemType.getCategory()) &&
            compareItemSets(itemSet, itemType.getItemSet()) &&
            this.rarity == itemType.getRarity() &&
            this.specialItemType == itemType.getSpecialItemType();
    }

    private boolean compareItemSets(ItemSet itemSet, ItemSet itemSet1) {
        if (itemSet == null && itemSet1 == null) return true;
        if (itemSet != null && itemSet1 != null) return itemSet.deepEquals(itemSet1);
        return false;
    }
}