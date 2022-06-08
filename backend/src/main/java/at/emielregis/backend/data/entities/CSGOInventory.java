package at.emielregis.backend.data.entities;


import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Map;

@Entity
public class CSGOInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<Item, Integer> items;

    public CSGOInventory() {

    }

    public Map<Item, Integer> getItems() {
        return items;
    }

    public void setItems(Map<Item, Integer> items) {
        this.items = items;
    }

    public static class CSGOInventoryBuilder {
        private Map<Item, Integer> items;

        private CSGOInventoryBuilder() {

        }

        public static CSGOInventoryBuilder create() {
            return new CSGOInventoryBuilder();
        }

        public CSGOInventoryBuilder withItems(Map<Item, Integer> items) {
            this.items = items;
            return this;
        }

        public CSGOInventory build() {
            CSGOInventory inv = new CSGOInventory();
            inv.setItems(items);
            return inv;
        }
    }
}
