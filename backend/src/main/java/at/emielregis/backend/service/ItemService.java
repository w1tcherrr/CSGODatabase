package at.emielregis.backend.service;

import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.repository.ItemRepository;
import at.emielregis.backend.service.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemRepository itemRepository;
    private final Mapper mapper;

    public ItemService(ItemRepository itemRepository,
                       Mapper mapper) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
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
}