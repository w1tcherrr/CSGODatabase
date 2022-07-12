package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.repository.ItemNameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Component
public class ItemNameService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemNameRepository itemNameRepository;

    public ItemNameService(ItemNameRepository itemNameRepository) {
        this.itemNameRepository = itemNameRepository;
    }

    public List<ItemName> getSearch(String filter) {
        return itemNameRepository.getSearch(filter);
    }

    public long count() {
        LOGGER.info("ItemNameService#count()");
        return itemNameRepository.count();
    }

    public List<ItemName> getUnclassifiedStickerNames() {
        return itemNameRepository.getUnclassifiedStickerNames();
    }
}
