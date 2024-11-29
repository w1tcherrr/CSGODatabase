package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.enums.Rarity;
import at.emielregis.backend.repository.ItemNameRepository;
import at.emielregis.backend.repository.ItemTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemNameService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemNameRepository itemNameRepository;
    private final ItemTypeRepository itemTypeRepository;

    public ItemNameService(ItemNameRepository itemNameRepository, ItemTypeRepository itemTypeRepository) {
        this.itemNameRepository = itemNameRepository;
        this.itemTypeRepository = itemTypeRepository;
    }

    public List<ItemName> getSearch(String... filters) {
        LOGGER.info("ItemNameService#getSearch(" + Arrays.toString(filters) + ")");
        List<ItemName> names = new ArrayList<>();
        for (String filter : filters) {
            names.addAll(itemNameRepository.getSearch(filter));
        }
        return names.stream().distinct().collect(Collectors.toList());
    }

    public long count() {
        LOGGER.info("ItemNameService#count()");
        return itemNameRepository.count();
    }

    public Rarity getRarityForItemNameName(String itemNameName) {
        LOGGER.info("ItemNameService#getRarityForItemNameName(" + itemNameName + ")");
        ItemName itemName = itemNameRepository.findByName(itemNameName);
        List<Rarity> rarities = itemTypeRepository.getRarityForItemName(itemName);
        rarities.sort(Comparator.comparingInt(rar -> itemTypeRepository.countForItemNameAndRarity(itemTypeRepository.getAllTypesForNames(List.of(itemName)), rar)));
        Collections.reverse(rarities);
        return rarities.get(0);
    }

    public boolean itemNameHasExteriors(ItemName itemName) {
        LOGGER.info("ItemService#itemNameHasExteriors(" + itemName.toString() + ")");
        return itemNameRepository.itemNameHasExteriors(itemName);
    }

    public List<ItemName> getAllNamesForSet(ItemSet set) {
        return itemNameRepository.getAllNamesForSet(set);
    }

    public List<ItemName> getAll() {
        return itemNameRepository.findAll();
    }
}
