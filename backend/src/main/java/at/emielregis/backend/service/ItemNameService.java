package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.enums.Rarity;
import at.emielregis.backend.repository.ItemNameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class ItemNameService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Map<ItemName, Rarity> rarities = new ConcurrentHashMap<>();

    private final ItemNameRepository itemNameRepository;

    public ItemNameService(ItemNameRepository itemNameRepository) {
        this.itemNameRepository = itemNameRepository;
    }

    public List<ItemName> getSearch(String... filters) {
        LOGGER.info("ItemNameService#getSearch(" + Arrays.toString(filters) + ")");
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
        LOGGER.info("ItemNameService#getUnclassifiedStickerNames()");
        return itemNameRepository.getUnclassifiedStickerNames();
    }

    public Rarity getRarityForItemNameName(String itemNameName) {
        LOGGER.info("ItemNameService#getRarityForItemNameName(" + itemNameName + ")");
        ItemName itemName = itemNameRepository.getByName(itemNameName);
        var rarity = rarities.get(itemName);
        if (rarity == null) {
            initRarities();
            return getRarityForItemNameName(itemNameName);
        }
        return rarity;
    }

    private void initRarities() {
        LOGGER.info("ItemNameService#initRarities()");
        AtomicInteger integer = new AtomicInteger();
        long size = itemNameRepository.count();
        itemNameRepository.findAll().parallelStream().forEach(
            name -> {
                LOGGER.info("Initializing rarity for name " + integer.incrementAndGet() + "/" + size + ": " + name.getName());
                List<Rarity> list = itemNameRepository.getRarityForItemName(name);
                if (list.size() > 1) {
                    LOGGER.info("Item with name :" + name.getName() + " has more than one rarity: " + list + ", will take the rarity which is found more often.");
                    list.sort(Comparator.comparingInt(rarity -> itemNameRepository.countByItemNameAndRarity(name, rarity)));
                    Collections.reverse(list);
                }
                rarities.put(name, list.get(0));
            }
        );
    }
}
