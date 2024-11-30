package at.emielregis.backend.data.entities.items;

import at.emielregis.backend.data.enums.StickerType;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents an applied sticker. Non-applied stickers are normal {@link ItemCollection}s.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, updatable = false, nullable = false)
    private String name;

    @Column(updatable = false, nullable = false)
    private StickerType stickerType;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Sticker sticker = (Sticker) o;
        return id != null && Objects.equals(id, sticker.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean deepEquals(Sticker sticker) {
        return this.stickerType == sticker.getStickerType() &&
            this.name.equals(sticker.getName());
    }
}
