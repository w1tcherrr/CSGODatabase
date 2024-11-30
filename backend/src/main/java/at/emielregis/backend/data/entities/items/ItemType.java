package at.emielregis.backend.data.entities.items;

import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.Rarity;
import at.emielregis.backend.data.enums.SpecialItemType;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents a unique type of item in the system, defined by its specific attributes such as name, exterior, rarity,
 * category, associated item set, special item type, and market hash name.
 * <p>
 * For example, all "Breakout Cases" will share the same {@link ItemType} since they do not differ in any attribute.
 * However, items with differing attributes, such as exteriors (e.g., "Factory New", "Field-Tested"), rarity levels, or
 * belonging to different {@link ItemSet}s, will have distinct {@link ItemType} entries to account for these variations.
 */

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

    @Column(updatable = false)
    private String marketHashName;

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
            Objects.equals(this.marketHashName, itemType.marketHashName) &&
            this.rarity == itemType.getRarity() &&
            this.specialItemType == itemType.getSpecialItemType() &&
            compareItemSets(itemSet, itemType.getItemSet());
    }

    private boolean compareItemSets(ItemSet itemSet, ItemSet itemSet1) {
        if (itemSet == null && itemSet1 == null) return true;
        if (itemSet != null && itemSet1 != null) return itemSet.deepEquals(itemSet1);
        return false;
    }
}
