package at.emielregis.backend.data.entities.items;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents a Charm entity with a unique name.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Charm {

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
        Charm charm = (Charm) o;
        return id != null && Objects.equals(id, charm.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean deepEquals(Charm charm) {
        return this.name.equals(charm.getName());
    }
}
