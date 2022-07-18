package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.entities.items.ItemType;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.SpecialItemType;
import at.emielregis.backend.repository.ItemTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Component
public class ItemTypeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemTypeRepository itemTypeRepository;

    public ItemTypeService(ItemTypeRepository itemTypeRepository) {
        this.itemTypeRepository = itemTypeRepository;
    }

    public List<ItemType> getUnclassifiedStickerTypes() {
        LOGGER.info("ItemNameService#getUnclassifiedStickerNames()");
        return itemTypeRepository.getUnclassifiedStickerTypes();
    }

    public List<ItemType> getAllTypesForSet(ItemSet set) {
        LOGGER.info("ItemService#getAllNamesForSet(" + set.toString() + ")");
        return itemTypeRepository.getAllTypesForSet(set);
    }

    public List<ItemType> getTypeForItemNameAndParams(ItemName itemName, Exterior exterior, SpecialItemType specialItemType) {
        return itemTypeRepository.getTypeForItemNameAndParams(itemName, exterior, specialItemType);
    }

    public ItemType getStorageUnitType() {
        return itemTypeRepository.getStorageUnitType();
    }

    public List<ItemType> getTypesForItemNames(List<ItemName> search) {
        return itemTypeRepository.getAllTypesForNames(search);
    }
}
