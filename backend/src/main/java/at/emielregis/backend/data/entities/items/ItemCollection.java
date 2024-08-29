package at.emielregis.backend.data.entities.items;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
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
        return compareStickers(item)
            && Objects.equals(this.nameTag, item.getNameTag())
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
            return this.stickers == null ? item.stickers.isEmpty() : this.stickers.isEmpty();
        }
    }
}
