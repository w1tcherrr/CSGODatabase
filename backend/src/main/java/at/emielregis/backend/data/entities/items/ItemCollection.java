package at.emielregis.backend.data.entities.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ItemType itemType;

    @Column(nullable = false, updatable = false)
    private int amount;

    @Column(updatable = false)
    private Integer storageUnitAmount;

    @Column(updatable = false)
    private String nameTag;

    @ManyToMany
    private List<Sticker> stickers;

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

    public boolean deepEquals(ItemCollection item) {
        return compareStickers(item) && Objects.equals(this.storageUnitAmount, item.getStorageUnitAmount()) &&
            Objects.equals(this.nameTag, item.getNameTag()) && this.getItemType().deepEquals(item.getItemType());
    }

    private boolean compareStickers(ItemCollection item) {
        if (this.stickers == null && item.getStickers() == null) {
            return true;
        }
        if (this.stickers != null && item.getStickers() != null) {
            if (this.stickers.size() != item.getStickers().size()) {
                return false;
            }
            for (int i = 0; i < this.stickers.size(); i++) {
                if (!this.stickers.get(i).deepEquals(item.getStickers().get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
