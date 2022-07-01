package at.emielregis.backend.service;

import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.repository.ItemRepository;
import at.emielregis.backend.service.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemRepository itemRepository;
    private final Mapper mapper;

    public ItemManager(ItemRepository itemRepository, Mapper mapper) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
    }

    public List<Item> convert(List<TransientItem> itemList) {
        LOGGER.info("Converting inventory");

        return itemList.stream().map(transientItem -> {
            Item item = mapper.mapAndSave(transientItem);
            return itemRepository.save(item);
        }).collect(Collectors.toList());
    }
}
