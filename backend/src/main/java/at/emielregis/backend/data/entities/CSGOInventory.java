package at.emielregis.backend.data.entities;


import at.emielregis.backend.data.entities.items.ItemCollection;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Represents the inventory of a CS:GO account, containing a collection of items.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CSGOInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    private List<ItemCollection> itemCollections;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CSGOInventory inventory = (CSGOInventory) o;
        return id != null && Objects.equals(id, inventory.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public long getTotalItemAmount() {
        return itemCollections.stream().mapToLong(ItemCollection::getAmount).sum();
    }
}
