package at.emielregis.backend.data.entities.items;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Represents a collection of items, including item type, stickers, and charm.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false)
    private ItemType itemType;

    @Column(nullable = false, updatable = false)
    private int amount;

    @Column(updatable = false)
    private String nameTag;

    @ManyToMany
    private List<Sticker> stickers;

    @ManyToOne
    private Charm charm;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ItemCollection that = (ItemCollection) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Deeply compares this ItemCollection with another, including stickers, name tag, charm, and item type.
     *
     * @param item The ItemCollection to compare with.
     * @return true if all properties are equal, false otherwise.
     */
    public boolean deepEquals(ItemCollection item) {
        return compareStickers(item)
            && Objects.equals(this.nameTag, item.getNameTag())
            && compareCharm(item)
            && this.getItemType().deepEquals(item.getItemType());
    }

    private boolean compareStickers(ItemCollection item) {
        if (this.stickers == null && item.getStickers() == null) {
            return true;
        } else if (this.stickers != null && item.getStickers() != null) {
            if (this.stickers.size() != item.getStickers().size()) {
                return false;
            }
            for (int i = 0; i < this.stickers.size(); i++) {
                if (!this.stickers.get(i).deepEquals(item.getStickers().get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return this.stickers == null ? item.getStickers().isEmpty() : this.stickers.isEmpty();
        }
    }

    private boolean compareCharm(ItemCollection item) {
        if (this.charm == null && item.getCharm() == null) {
            return true;
        } else if (this.charm != null && item.getCharm() != null) {
            return this.charm.deepEquals(item.getCharm());
        } else {
            return false;
        }
    }
}
