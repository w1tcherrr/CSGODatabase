package at.emielregis.backend.service;

import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemCategory;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.repository.ItemRepository;
import at.emielregis.backend.service.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
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

    public ItemService(ItemRepository itemRepository,
                       ItemCategoryService itemCategoryService,
                       Mapper mapper,
                       EntityManager entityManager) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
        this.itemCategoryService = itemCategoryService;
    }

    public void saveAll(List<Item> items) {
        LOGGER.info("ItemService#saveAll()");
        itemRepository.saveAll(items);
    }

    public Set<Long> getAllItemIDs() {
        LOGGER.info("ItemService#getAllItemIDs()");
        return itemRepository.getAllItemIDs();
    }

    public Set<Long> getNormalItemIDs() {
        LOGGER.info("ItemService#getNormalItemIDs()");
        return itemRepository.getNormalItemIDs();
    }

    public List<Item> convert(List<TransientItem> itemList) {
        LOGGER.info("ItemService#convert()");
        return itemList.stream().map(mapper::map).collect(Collectors.toList());
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

    public long itemCountNoStorageUnits() {
        LOGGER.info("ItemService#itemCountNoStorageUnits()");
        return itemRepository.itemCountNoStorageUnits();
    }

    public long itemCountOnlyStorageUnits() {
        LOGGER.info("ItemService#itemCountOnlyStorageUnits()");
        return itemRepository.itemCountInStorageUnits();
    }

    public long getHighestSingleInventoryCount() {
        LOGGER.info("ItemService#getHighestSingleInventoryCount()");
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(case when i.storageUnitAmount IS NOT NULL then " +
                "(i.storageUnitAmount * i.amount + 1) else i.amount end) as amount from CSGOInventory inv join inv.items i group by inv.id order by amount desc",
            Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public long getLowestSingleInventoryCount() {
        LOGGER.info("ItemService#getLowestSingleInventoryCount()");
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(case when i.storageUnitAmount IS NOT NULL then " +
                "(i.storageUnitAmount * i.amount + 1) else i.amount end) as amount from CSGOInventory inv join inv.items i group by inv.id order by amount asc",
            Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public long getHighestStorageUnitCount() {
        LOGGER.info("ItemService#getHighestStorageUnitCount()");
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(i.amount) as c from CSGOInventory inv join inv.items i where i.storageUnitAmount IS NOT NULL group by inv.id order by c desc", Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public long getHighestFullStorageUnitCount() {
        LOGGER.info("ItemService#getHighestFullStorageUnitCount()");
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(i.amount) as c from CSGOInventory inv join inv.items i where i.storageUnitAmount = 1000 group by inv.id order by c desc", Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public List<ItemName> getAllNamesForSet(ItemSet set) {
        LOGGER.info("ItemService#getAllNamesForSet(" + set.toString() + ")");
        return itemRepository.getAllNamesForSet(set);
    }

    public long getTotalAmountForName(ItemName itemName) {
        LOGGER.info("ItemService#getTotalAmountForName(" + itemName.toString() + ")");
        return itemRepository.getTotalAmountForName(itemName);
    }

    public long getSouvenirOrStatTrakAmountForName(ItemName itemName) {
        LOGGER.info("ItemService#getSouvenirOrStatTrakAmountForName(" + itemName.toString() + ")");
        Long amount = itemRepository.getSouvenirOrStatTrakAmountForName(itemName);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public boolean itemNameHasExteriors(ItemName itemName) {
        LOGGER.info("ItemService#itemNameHasExteriors(" + itemName.toString() + ")");
        return itemRepository.itemNameHasExteriors(itemName);
    }

    public long countForExteriorAndType(ItemName itemName, Exterior exterior, boolean statTrak, boolean souvenir) {
        LOGGER.info("ItemService#countForExteriorAndType(" + itemName.toString() + ", " + exterior.toString() + ", " + statTrak + ", " + souvenir + ")");
        Long amount = itemRepository.countForExteriorAndType(itemName, exterior, statTrak, souvenir);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getTotalAmountForSetNoContainers(ItemSet set) {
        LOGGER.info("ItemService#getTotalAmountForSetNoContainers(" + set.toString() + ")");
        List<ItemCategory> containerCategories = itemCategoryService.getAllContainerCategories();
        Long amount = itemRepository.countForSetNoContainers(set, containerCategories);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getTotalAmountOfContainersForSet(ItemSet set) {
        LOGGER.info("ItemService#getTotalAmountOfContainersForSet(" + set.toString() + ")");
        List<ItemCategory> containerCategories = itemCategoryService.getAllContainerCategories();
        Long amount = itemRepository.countContainersForSet(set, containerCategories);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public long getNormalItemCount() {
        LOGGER.info("ItemService#getNormalItemCount()");
        return itemRepository.normalItemCount();
    }

    public long getTotalAmountOfEmptyStorageUnits() {
        LOGGER.info("ItemService#getTotalAmountOfEmptyStorageUnits()");
        return itemRepository.getTotalAmountOfStorageUnitsWithNoName();
    }

    public List<Item> getAllNonEmptyStorageUnits() {
        LOGGER.info("ItemService#getAllNonEmptyStorageUnits()");
        return itemRepository.getAllNonEmptyStorageUnits();
    }

    public long getTotalAmountForNames(List<ItemName> search) {
        LOGGER.info("ItemService#getTotalAmountForNames(" + search + ")");
        return itemRepository.getTotalAmountForNames(search);
    }
}