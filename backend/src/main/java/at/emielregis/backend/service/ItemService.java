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

@Component
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemRepository itemRepository;
    private final Mapper mapper;
    private final ItemCategoryService itemCategoryService;
    private final ItemTypeService itemTypeService;

    public ItemService(ItemRepository itemRepository,
                       ItemCategoryService itemCategoryService,
                       Mapper mapper,
                       ItemTypeService itemTypeService) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
        this.itemCategoryService = itemCategoryService;
        this.itemTypeService = itemTypeService;
    }

    public void saveAll(List<ItemCollection> itemCollections) {
        LOGGER.info("ItemService#saveAll()");
        itemRepository.saveAllAndFlush(itemCollections);
    }

    public List<ItemCollection> convert(List<ItemCollection> itemList) {
        LOGGER.info("ItemService#convert()");
        return itemList.stream().map(mapper::convertToNonTransient).collect(Collectors.toList());
    }

    public long count() {
        LOGGER.info("ItemService#count()");
        return itemRepository.count();
    }

    public long countTotalItems() {
        LOGGER.info("ItemService#countTotalItems()");
        return itemRepository.countTotalItems();
    }

    public long getTotalAmountForName(ItemName itemName) {
        LOGGER.info("ItemService#getTotalAmountForName(" + itemName.toString() + ")");
        List<ItemType> itemTypes = itemTypeService.getTypeForItemNameAndParams(itemName, null, null);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long countForExteriorAndType(ItemName itemName, Exterior exterior, boolean statTrak, boolean souvenir) {
        LOGGER.info("ItemService#countForExteriorAndType(" + itemName.toString() + ", " + exterior.toString() + ", " + statTrak + ", " + souvenir + ")");
        List<ItemType> itemTypes = itemTypeService.getTypeForItemNameAndParams(itemName, exterior, SpecialItemType.fromBooleans(statTrak, souvenir));
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getTotalAmountOfNonContainersForSet(ItemSet set) {
        LOGGER.info("ItemService#getTotalAmountForSetNoContainers(" + set.toString() + ")");
        List<ItemType> itemTypes = itemTypeService.getAllTypesForSet(set);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        List<ItemCategory> containerCategories = itemCategoryService.getAllContainerCategories();
        itemTypes.removeIf(type -> containerCategories.contains(type.getCategory()));
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getTotalAmountOfContainersForSet(ItemSet set) {
        LOGGER.info("ItemService#getTotalAmountForSetNoContainers(" + set.toString() + ")");
        List<ItemType> itemTypes = itemTypeService.getAllTypesForSet(set);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        List<ItemCategory> containerCategories = itemCategoryService.getAllContainerCategories();
        itemTypes.removeIf(type -> !containerCategories.contains(type.getCategory()));
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getTotalAmountForNames(List<ItemName> search) {
        LOGGER.info("ItemService#getTotalAmountForNames(" + search + ")");
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(search);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getTotalAmountOfContainersForNames(List<ItemName> search) {
        LOGGER.info("ItemService#getTotalAmountOfContainersForNames(" + search + ")");
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(search);
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        List<ItemCategory> containerCategories = itemCategoryService.getAllContainerCategories();
        itemTypes.removeIf(type -> !containerCategories.contains(type.getCategory()));
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getSouvenirAmountForName(ItemName itemName) {
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(List.of(itemName));
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        itemTypes.removeIf(type -> type.getSpecialItemType() != SpecialItemType.SOUVENIR);
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getStatTrakAmountForName(ItemName itemName) {
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(List.of(itemName));
        if (itemTypes == null || itemTypes.isEmpty()) {
            return 0;
        }
        itemTypes.removeIf(type -> type.getSpecialItemType() != SpecialItemType.STAT_TRAK);
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public Set<Long> getOrphanedIDs() {
        return itemRepository.getOrphanedItemIds();
    }

    public int getTotalAmountForType(ItemType type) {
        return itemRepository.getTotalAmountForType(type);
    }

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