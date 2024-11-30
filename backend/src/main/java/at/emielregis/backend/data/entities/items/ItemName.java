package at.emielregis.backend.data.entities.items;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents the name of an item (e.g. "M4A1-S | Knight")
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ItemName {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, updatable = false, nullable = false)
    private String name;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ItemName itemName = (ItemName) o;
        return id != null && Objects.equals(id, itemName.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean deepEquals(ItemName name) {
        return this.name.equals(name.getName());
    }
}
