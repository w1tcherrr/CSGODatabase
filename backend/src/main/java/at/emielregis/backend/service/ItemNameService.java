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

/**
 * Service class for managing {@link ItemName} entities.
 * Provides methods to search, retrieve rarity, and manage exterior-related attributes for item names.
 */
@Component
public class ItemNameService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemNameRepository itemNameRepository;
    private final ItemTypeRepository itemTypeRepository;

    /**
     * Constructs the service with the required repositories.
     *
     * @param itemNameRepository The repository for managing {@link ItemName} entities.
     * @param itemTypeRepository The repository for managing {@link at.emielregis.backend.data.entities.items.ItemType} entities.
     */
    public ItemNameService(ItemNameRepository itemNameRepository, ItemTypeRepository itemTypeRepository) {
        this.itemNameRepository = itemNameRepository;
        this.itemTypeRepository = itemTypeRepository;
    }

    /**
     * Searches for item names based on a set of filters.
     * Ensures the results are distinct.
     *
     * @param filters The filters to search item names by.
     * @return A list of distinct item names matching the filters.
     */
    public List<ItemName> getSearch(String... filters) {
        LOGGER.info("ItemNameService#getSearch(" + Arrays.toString(filters) + ")");
        List<ItemName> names = new ArrayList<>();
        for (String filter : filters) {
            names.addAll(itemNameRepository.getSearch(filter));
        }
        return names.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Counts the total number of item names in the database.
     *
     * @return The total count of item names.
     */
    public long count() {
        LOGGER.info("ItemNameService#count()");
        return itemNameRepository.count();
    }

    /**
     * Determines the most frequent rarity for a given item name.
     * Sorts rarities by their frequency, selecting the highest occurrence.
     *
     * @param itemNameName The name of the item to retrieve rarity for.
     * @return The most frequent {@link Rarity} for the item.
     */
    public Rarity getRarityForItemNameName(String itemNameName) {
        LOGGER.info("ItemNameService#getRarityForItemNameName(" + itemNameName + ")");
        ItemName itemName = itemNameRepository.findByName(itemNameName);
        List<Rarity> rarities = itemTypeRepository.getRarityForItemName(itemName);
        rarities.sort(Comparator.comparingInt(rar ->
            itemTypeRepository.countForItemNameAndRarity(
                itemTypeRepository.getAllTypesForNames(List.of(itemName)), rar)
        ));
        Collections.reverse(rarities);
        return rarities.get(0);
    }

    /**
     * Checks whether an item name is associated with any exteriors.
     *
     * @param itemName The {@link ItemName} to check.
     * @return True if the item name has exteriors, false otherwise.
     */
    public boolean itemNameHasExteriors(ItemName itemName) {
        LOGGER.info("ItemNameService#itemNameHasExteriors(" + itemName.toString() + ")");
        return itemNameRepository.itemNameHasExteriors(itemName);
    }

    /**
     * Retrieves all item names associated with a given set.
     *
     * @param set The {@link ItemSet} to fetch item names for.
     * @return A list of item names associated with the set.
     */
    public List<ItemName> getAllNamesForSet(ItemSet set) {
        LOGGER.info("ItemNameService#getAllNamesForSet(" + set.toString() + ")");
        return itemNameRepository.getAllNamesForSet(set);
    }

    /**
     * Retrieves all item names from the database.
     *
     * @return A list of all {@link ItemName} entities.
     */
    public List<ItemName> getAll() {
        LOGGER.info("ItemNameService#getAll()");
        return itemNameRepository.findAll();
    }
}
