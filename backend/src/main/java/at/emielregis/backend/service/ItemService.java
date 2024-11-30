package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.*;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.SpecialItemType;
import at.emielregis.backend.repository.ItemRepository;
import at.emielregis.backend.service.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing {@link ItemCollection} entities.
 * This class provides methods to handle the saving, retrieval, conversion, and analysis of item data.
 * It interacts with the database through the {@link ItemRepository} and other related services.
 */
@Component
public class ItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemRepository itemRepository;
    private final Mapper mapper;
    private final ItemCategoryService itemCategoryService;
    private final ItemTypeService itemTypeService;

    /**
     * Constructor for {@link ItemService}.
     *
     * @param itemRepository      The repository for accessing {@link ItemCollection} entities.
     * @param itemCategoryService Service for handling item categories.
     * @param mapper              Mapper for converting transient entities to database entities.
     * @param itemTypeService     Service for managing item types.
     */
    public ItemService(ItemRepository itemRepository,
                       ItemCategoryService itemCategoryService,
                       Mapper mapper,
                       ItemTypeService itemTypeService) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
        this.itemCategoryService = itemCategoryService;
        this.itemTypeService = itemTypeService;
    }

    /**
     * Saves a list of {@link ItemCollection} entities to the database.
     *
     * @param itemCollections The list of items to be saved.
     */
    public void saveAll(List<ItemCollection> itemCollections) {
        LOGGER.info("ItemService#saveAll({})", itemCollections.size());
        itemRepository.saveAllAndFlush(itemCollections);
    }

    /**
     * Converts a list of transient {@link ItemCollection} objects to persistent entities.
     *
     * @param itemList The list of transient item collections to convert.
     * @return The list of converted item collections.
     */
    public List<ItemCollection> convert(List<ItemCollection> itemList) {
        LOGGER.info("ItemService#convert({})", itemList.size());
        return itemList.stream().map(mapper::convertToNonTransient).collect(Collectors.toList());
    }

    /**
     * Counts the total number of item collections in the database.
     *
     * @return The total count of item collections.
     */
    public long count() {
        LOGGER.info("ItemService#count()");
        return itemRepository.count();
    }

    /**
     * Counts the total number of items across all item collections.
     *
     * @return The total count of items.
     */
    public long countTotalItems() {
        LOGGER.info("ItemService#countTotalItems()");
        return itemRepository.countTotalItems();
    }

    /**
     * Retrieves the total amount of items for a specific item name.
     *
     * @param itemName The {@link ItemName} for which to retrieve the total amount.
     * @return The total amount of items for the specified name.
     */
    public long getTotalAmountForName(ItemName itemName) {
        LOGGER.info("ItemService#getTotalAmountForName({})", itemName.getName());
        List<ItemType> itemTypes = itemTypeService.getTypeForItemNameAndParams(itemName, null, null);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        return amount == null ? 0 : amount;
    }

    /**
     * Counts the number of items for a specific exterior and type.
     *
     * @param itemName The {@link ItemName}.
     * @param exterior The {@link Exterior}.
     * @param statTrak Whether the item is StatTrak.
     * @param souvenir Whether the item is Souvenir.
     * @return The count of items matching the criteria.
     */
    public long countForExteriorAndType(ItemName itemName, Exterior exterior, boolean statTrak, boolean souvenir) {
        LOGGER.info("ItemService#countForExteriorAndType({}, {}, {}, {})", itemName.getName(), exterior, statTrak, souvenir);
        List<ItemType> itemTypes = itemTypeService.getTypeForItemNameAndParams(itemName, exterior, SpecialItemType.fromBooleans(statTrak, souvenir));
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        return amount == null ? 0 : amount;
    }

    /**
     * Retrieves the total amount of non-container items in a specific item set.
     *
     * @param set The {@link ItemSet}.
     * @return The total amount of non-container items.
     */
    public long getTotalAmountOfNonContainersForSet(ItemSet set) {
        LOGGER.info("ItemService#getTotalAmountOfNonContainersForSet({})", set.getName());
        List<ItemType> itemTypes = itemTypeService.getAllTypesForSet(set);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        List<ItemCategory> containerCategories = itemCategoryService.getAllContainerCategories();
        itemTypes.removeIf(type -> containerCategories.contains(type.getCategory()));
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        return amount == null ? 0 : amount;
    }

    /**
     * Retrieves the total amount of container items in a specific item set.
     *
     * @param set The {@link ItemSet}.
     * @return The total amount of container items.
     */
    public long getTotalAmountOfContainersForSet(ItemSet set) {
        LOGGER.info("ItemService#getTotalAmountOfContainersForSet({})", set.getName());
        List<ItemType> itemTypes = itemTypeService.getAllTypesForSet(set);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        List<ItemCategory> containerCategories = itemCategoryService.getAllContainerCategories();
        itemTypes.removeIf(type -> !containerCategories.contains(type.getCategory()));
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        return amount == null ? 0 : amount;
    }

    /**
     * Retrieves the total amount of items for a list of item names.
     *
     * @param search The list of {@link ItemName} objects to search for.
     * @return The total amount of items.
     */
    public long getTotalAmountForNames(List<ItemName> search) {
        LOGGER.info("ItemService#getTotalAmountForNames({})", search);
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(search);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        return amount == null ? 0 : amount;
    }

    /**
     * Retrieves the total amount of container items for a list of item names.
     *
     * @param search The list of {@link ItemName} objects.
     * @return The total amount of container items.
     */
    public long getTotalAmountOfContainersForNames(List<ItemName> search) {
        LOGGER.info("ItemService#getTotalAmountOfContainersForNames({})", search);
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(search);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        List<ItemCategory> containerCategories = itemCategoryService.getAllContainerCategories();
        itemTypes.removeIf(type -> !containerCategories.contains(type.getCategory()));
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        return amount == null ? 0 : amount;
    }

    /**
     * Retrieves the total amount of Souvenir items for a specific item name.
     *
     * @param itemName The {@link ItemName}.
     * @return The total amount of Souvenir items.
     */
    public long getSouvenirAmountForName(ItemName itemName) {
        LOGGER.info("ItemService#getSouvenirAmountForName({})", itemName.getName());
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(List.of(itemName));
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        itemTypes.removeIf(type -> type.getSpecialItemType() != SpecialItemType.SOUVENIR);
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        return amount == null ? 0 : amount;
    }

    /**
     * Retrieves the total amount of StatTrak items for a specific item name.
     *
     * @param itemName The {@link ItemName}.
     * @return The total amount of StatTrak items.
     */
    public long getStatTrakAmountForName(ItemName itemName) {
        LOGGER.info("ItemService#getStatTrakAmountForName({})", itemName.getName());
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(List.of(itemName));
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        itemTypes.removeIf(type -> type.getSpecialItemType() != SpecialItemType.STAT_TRAK);
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        return amount == null ? 0 : amount;
    }

    /**
     * Retrieves IDs of orphaned items (items not associated with any inventory).
     *
     * @return A set of orphaned item IDs.
     */
    public Set<Long> getOrphanedIDs() {
        LOGGER.info("ItemService#getOrphanedIDs()");
        return itemRepository.getOrphanedItemIds();
    }

    /**
     * Retrieves the total amount of items for a specific item type.
     *
     * @param type The {@link ItemType}.
     * @return The total amount of items for the specified type.
     */
    public int getTotalAmountForType(ItemType type) {
        LOGGER.info("ItemService#getTotalAmountForType({})", type.getId());
        return itemRepository.getTotalAmountForType(type);
    }

    /**
     * Retrieves a map of name tags and their counts across all item collections.
     *
     * @return A map where the key is the name tag, and the value is its count.
     */
    public Map<String, Integer> getNameTagMap() {
        LOGGER.info("ItemService#getNameTagMap()");
        List<Object[]> nameTagCounts = itemRepository.getNameTagCounts();
        return nameTagCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Long) row[1]).intValue()
            ));
    }
}
