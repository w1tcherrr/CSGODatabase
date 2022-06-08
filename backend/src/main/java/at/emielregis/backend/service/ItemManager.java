package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemType;
import at.emielregis.backend.repository.ItemRepository;
import at.emielregis.backend.repository.ItemTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

@Component
public class ItemManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ItemRepository itemRepository;
    public ItemTypeRepository itemTypeRepository;

    public ItemManager(ItemRepository itemRepository, ItemTypeRepository itemTypeRepository) {
        this.itemRepository = itemRepository;
        this.itemTypeRepository = itemTypeRepository;
    }

    public Map<Item, Integer> convert(Map<String, Integer> inventoryMap, Map<String, String> typeMap) {
        LOGGER.info("Converting inventory");
        Map<Item, Integer> itemMap = new HashMap<>();

        inventoryMap.forEach((key, value) -> {
            if (itemRepository.existsByName(key)) {
                Item item = itemRepository.getByName(key);
                itemMap.put(item, value);
            } else {
                ItemType itemType;
                if (itemTypeRepository.existsByName(typeMap.get(key))) {
                    itemType = itemTypeRepository.getByName(typeMap.get(key));
                } else {
                    ItemType.ItemTypeBuilder itemTypeBuilder = ItemType.ItemTypeBuilder.create();
                    itemTypeBuilder.withName(typeMap.get(key));
                    itemType = itemTypeRepository.save(itemTypeBuilder.build());
                }
                Item.ItemBuilder builder = Item.ItemBuilder.create();
                builder.withName(key);
                builder.withItemType(itemType);
                Item item = itemRepository.save(builder.build());
                itemMap.put(item, value);
            }
        });

        return itemMap;
    }
}
