package at.emielregis.backend.data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    private ItemType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public static class ItemBuilder {
        private String name;
        private ItemType itemType;

        private ItemBuilder() {

        }

        public static ItemBuilder create() {
            return new ItemBuilder();
        }

        public ItemBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ItemBuilder withItemType(ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        public Item build() {
            Item item = new Item();
            item.setName(name);
            item.setType(itemType);
            return item;
        }
    }
}
