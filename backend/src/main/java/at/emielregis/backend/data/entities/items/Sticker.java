package at.emielregis.backend.data.entities.items;

import at.emielregis.backend.data.enums.StickerType;
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
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
