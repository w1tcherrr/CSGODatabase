package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.ItemCategory;
import at.emielregis.backend.data.entities.items.ItemCollection;
import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.entities.items.ItemType;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.SpecialItemType;
import at.emielregis.backend.repository.ItemRepository;
import at.emielregis.backend.service.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemRepository itemRepository;
    private final Mapper mapper;
    private final EntityManager entityManager;
    private final ItemCategoryService itemCategoryService;
    private final ItemTypeService itemTypeService;

    public ItemService(ItemRepository itemRepository,
                       ItemCategoryService itemCategoryService,
                       Mapper mapper,
                       EntityManager entityManager,
                       ItemTypeService itemTypeService) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
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

    @Transactional
    public void deleteAllById(Set<Long> orphanedIDs) {
        LOGGER.info("ItemService#deleteAllById(" + orphanedIDs + ")");
        for (Long id : orphanedIDs) {
            itemRepository.deleteById(id);
        }
        itemRepository.flush();
    }

    public long count() {
        LOGGER.info("ItemService#count()");
        return itemRepository.count();
    }

    public long countNormalItems() {
        LOGGER.info("ItemService#countNormalItems()");
        return itemRepository.normalItemCount();
    }

    public long countItemsNoStorageUnits() {
        LOGGER.info("ItemService#countItemsNoStorageUnits()");
        return itemRepository.itemCountNoStorageUnits();
    }

    public long countItemsInStorageUnits() {
        LOGGER.info("ItemService#countItemsInStorageUnits()");
        Long amount = itemRepository.itemCountInStorageUnits();
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getHighestSingleInventoryCount() {
        LOGGER.info("ItemService#getHighestSingleInventoryCount()");
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(case when i.storageUnitAmount IS NOT NULL then " +
                "(i.storageUnitAmount * i.amount + 1) else i.amount end) as amount from CSGOInventory inv join inv.itemCollections i group by inv.id order by amount desc",
            Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public long getHighestStorageUnitCount() {
        LOGGER.info("ItemService#getHighestStorageUnitCount()");
        try {
            TypedQuery<Long> query = entityManager.createQuery("SELECT sum(i.amount) as c from CSGOInventory inv join inv.itemCollections i where i.storageUnitAmount IS NOT NULL group by inv.id order by c desc", Long.class);
            query.setFirstResult(0).setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    public long getHighestFullStorageUnitCount() {
        LOGGER.info("ItemService#getHighestStorageUnitCount()");
        try {
            TypedQuery<Long> query = entityManager.createQuery("SELECT sum(i.amount) as c from CSGOInventory inv join inv.itemCollections i where i.storageUnitAmount = 1000 group by inv.id order by c desc", Long.class);
            query.setFirstResult(0).setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    public long getTotalAmountForName(ItemName itemName) {
        LOGGER.info("ItemService#getTotalAmountForName(" + itemName.toString() + ")");
        List<ItemType> itemTypes = itemTypeService.getTypeForItemNameAndParams(itemName, null, null);
        if (itemTypes == null || itemTypes.size() == 0) {
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
        if (itemTypes == null || itemTypes.size() == 0) {
            return 0;
        }
        Long amount = itemRepository.sumForItemTypes(itemTypes);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getTotalAmountForSetNoContainers(ItemSet set) {
        LOGGER.info("ItemService#getTotalAmountForSetNoContainers(" + set.toString() + ")");
        List<ItemType> itemTypes = itemTypeService.getAllTypesForSet(set);
        if (itemTypes == null || itemTypes.size() == 0) {
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
        if (itemTypes == null || itemTypes.size() == 0) {
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

    public long countAmountOfEmptyStorageUnits() {
        LOGGER.info("ItemService#getTotalAmountOfEmptyStorageUnits()");
        ItemType storageUnitType = itemTypeService.getStorageUnitType();
        return itemRepository.countEmptyStorageUnits(storageUnitType);
    }

    public List<ItemCollection> getAllNonEmptyStorageUnits() {
        LOGGER.info("ItemService#getAllNonEmptyStorageUnits()");
        ItemType storageUnitType = itemTypeService.getStorageUnitType();
        return itemRepository.getAllNonEmptyStorageUnits(storageUnitType);
    }

    public long getTotalAmountForNames(List<ItemName> search) {
        LOGGER.info("ItemService#getTotalAmountForNames(" + search + ")");
        List<ItemType> itemTypes = itemTypeService.getTypesForItemNames(search);
        if (itemTypes == null || itemTypes.size() == 0) {
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
        if (itemTypes == null || itemTypes.size() == 0) {
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
        if (itemTypes == null || itemTypes.size() == 0) {
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
        if (itemTypes == null || itemTypes.size() == 0) {
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
}