package at.emielregis.backend.data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class ItemTypeBuilder {
        private String name;

        private ItemTypeBuilder() {

        }

        public static ItemTypeBuilder create() {
            return new ItemTypeBuilder();
        }

        public ItemTypeBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ItemType build() {
            ItemType itemType = new ItemType();
            itemType.setName(name);
            return itemType;
        }
    }
}
