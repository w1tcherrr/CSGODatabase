package at.emielregis.backend.service;

import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.entities.ItemSet;
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

    public ItemService(ItemRepository itemRepository,
                       Mapper mapper,
                       EntityManager entityManager) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    public void saveAll(List<Item> items) {
        itemRepository.saveAll(items);
    }

    public List<Item> getItemsForName(ItemName itemName) {
        return itemRepository.getItemsForName(itemName);
    }

    public Set<Long> getAllItemIDs() {
        return itemRepository.getAllItemIDs();
    }

    public Set<Long> getNormalItemIDs() {
        return itemRepository.getNormalItemIDs();
    }

    public List<Item> convert(List<TransientItem> itemList) {
        LOGGER.info("Converting inventory");

        return itemList.stream().map(mapper::map).collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllById(Set<Long> orphanedIDs) {
        itemRepository.deleteAllById(orphanedIDs);
    }

    public Long count() {
        return itemRepository.count();
    }

    public long itemCountNoStorageUnits() {
        return itemRepository.itemCountNoStorageUnits();
    }

    public long itemCountOnlyStorageUnits() {
        return itemRepository.itemCountOnlyStorageUnits();
    }

    public long getHighestSingleInventoryCount() {
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(case when i.storageUnitAmount IS NOT NULL then " +
                "(i.storageUnitAmount * i.amount) else i.amount end) as amount from CSGOInventory inv join inv.items i group by inv.id order by amount desc",
            Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public long getLowestSingleInventoryCount() {
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(case when i.storageUnitAmount IS NOT NULL then " +
                "(i.storageUnitAmount * i.amount) else i.amount end) as amount from CSGOInventory inv join inv.items i group by inv.id order by amount asc",
            Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public long getHighestStorageUnitCount() {
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(i.amount) as c from CSGOInventory inv join inv.items i where i.storageUnitAmount IS NOT NULL group by inv.id order by c desc", Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public long getHighestFullStorageUnitCount() {
        TypedQuery<Long> query = entityManager.createQuery("SELECT sum(i.amount) as c from CSGOInventory inv join inv.items i where i.storageUnitAmount = 1000 group by inv.id order by c desc", Long.class);
        query.setFirstResult(0).setMaxResults(1);
        return query.getSingleResult();
    }

    public List<ItemName> getAllNamesForSet(ItemSet set) {
        return itemRepository.getAllNamesForSet(set);
    }
}