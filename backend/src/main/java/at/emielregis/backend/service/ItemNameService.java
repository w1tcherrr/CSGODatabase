package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.repository.ItemNameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemNameService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemNameRepository itemNameRepository;

    public ItemNameService(ItemNameRepository itemNameRepository) {
        this.itemNameRepository = itemNameRepository;
    }

    public List<ItemName> getSearch(String... filters) {
        List<ItemName> names = new ArrayList<>();
        for (String filter : filters) {
            names.addAll(itemNameRepository.getSearch(filter));
        }
        return names.stream().distinct().collect(Collectors.toList());
    }

    public long count() {
        LOGGER.info("ItemNameService#count()");
        return itemNameRepository.count();
    }

    public List<ItemName> getUnclassifiedStickerNames() {
        return itemNameRepository.getUnclassifiedStickerNames();
    }
}
