package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.*;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.SpecialItemType;
import at.emielregis.backend.repository.ItemTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Service class for managing {@link ItemType} entities.
 * Provides methods to retrieve item types based on various criteria.
 */
@Component
public class ItemTypeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemTypeRepository itemTypeRepository;

    /**
     * Constructs a new {@link ItemTypeService} with the required repositories.
     *
     * @param itemTypeRepository Repository for {@link ItemType} entities.
     */
    public ItemTypeService(ItemTypeRepository itemTypeRepository) {
        this.itemTypeRepository = itemTypeRepository;
    }

    /**
     * Retrieves a list of unclassified sticker item types.
     *
     * @return A list of unclassified sticker {@link ItemType} entities.
     */
    public List<ItemType> getUnclassifiedStickerTypes() {
        LOGGER.info("ItemTypeService#getUnclassifiedStickerTypes()");
        return itemTypeRepository.getUnclassifiedStickerTypes();
    }

    /**
     * Retrieves all item types associated with a specific item set.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return A list of {@link ItemType} entities associated with the item set.
     */
    public List<ItemType> getAllTypesForSet(ItemSet set) {
        LOGGER.info("ItemTypeService#getAllTypesForSet({})", set);
        return itemTypeRepository.getAllTypesForSet(set);
    }

    /**
     * Retrieves all item names associated with a specific item set.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return A list of {@link ItemName} entities associated with the item set.
     */
    public List<ItemName> getAllNamesForSet(ItemSet set) {
        LOGGER.info("ItemTypeService#getAllNamesForSet({})", set);
        return itemTypeRepository.getAllNamesForSet(set);
    }

    /**
     * Retrieves item types based on item name, exterior, and special item type.
     *
     * @param itemName        The {@link ItemName} to filter by.
     * @param exterior        The {@link Exterior} to filter by (nullable).
     * @param specialItemType The {@link SpecialItemType} to filter by (nullable).
     * @return A list of matching {@link ItemType} entities.
     */
    public List<ItemType> getTypeForItemNameAndParams(ItemName itemName, Exterior exterior, SpecialItemType specialItemType) {
        LOGGER.info("ItemTypeService#getTypeForItemNameAndParams({}, {}, {})", itemName, exterior, specialItemType);
        return itemTypeRepository.getTypeForItemNameAndParams(itemName, exterior, specialItemType);
    }

    /**
     * Retrieves item types for a list of item names.
     *
     * @param search A list of {@link ItemName} entities to filter by.
     * @return A list of matching {@link ItemType} entities.
     */
    public List<ItemType> getTypesForItemNames(List<ItemName> search) {
        LOGGER.info("ItemTypeService#getTypesForItemNames({})", search);
        return itemTypeRepository.getAllTypesForNames(search);
    }

    public List<ItemName> getCharmItemNamesBySet(ItemSet set) {
        LOGGER.info("ItemTypeService#getCharmItemNamesBySet({})", set.getName());
        return itemTypeRepository.getCharmItemNamesBySet(set);
    }

    public List<ItemName> getUnclassifiedCharmItemNames() {
        LOGGER.info("ItemTypeService#getUnclassifiedCharmItemNames()");
        return itemTypeRepository.getUnclassifiedCharmItemNames();
    }
}
