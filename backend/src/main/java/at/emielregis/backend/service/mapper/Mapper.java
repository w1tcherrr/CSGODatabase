package at.emielregis.backend.service.mapper;

import at.emielregis.backend.data.dtos.TransientItem;
import at.emielregis.backend.data.dtos.TransientItemCategory;
import at.emielregis.backend.data.dtos.TransientItemSet;
import at.emielregis.backend.data.dtos.TransientSticker;
import at.emielregis.backend.data.entities.ClassID;
import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemCategory;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.data.entities.Sticker;
import at.emielregis.backend.repository.ClassIdRepository;
import at.emielregis.backend.repository.ItemCategoryRepository;
import at.emielregis.backend.repository.ItemNameRepository;
import at.emielregis.backend.repository.ItemSetRepository;
import at.emielregis.backend.repository.ItemTypeRepository;
import at.emielregis.backend.repository.StickerRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public record Mapper(ItemTypeRepository itemTypeRepository,
                     StickerRepository stickerRepository,
                     ItemCategoryRepository itemCategoryRepository,
                     ItemNameRepository itemNameRepository,
                     ClassIdRepository classIdRepository,
                     ItemSetRepository itemSetRepository) {

    /**
     * Maps a transient item to an entity item.
     * Stores the sub-entities into the database already, such as ItemName, Stickers, etc.
     *
     * @param transientItem The item to be mapped.
     * @return The database entity (not stored in the database yet!).
     */
    public synchronized Item map(TransientItem transientItem) {
        return Item.builder()
            .classID(mapClassId(transientItem.getClassID()))
            .amount(transientItem.getAmount())
            .name(map(transientItem.getName()))
            .nameTag(transientItem.getNameTag())
            .tradable(transientItem.isTradable())
            .statTrak(transientItem.isStatTrak())
            .souvenir(transientItem.isSouvenir())
            .category(map(transientItem.getCategory()))
            .stickers(map(transientItem.getStickers()))
            .exterior(transientItem.getExterior())
            .itemSet(map(transientItem.getItemSet()))
            .rarity(transientItem.getRarity())
            .storageUnitAmount(transientItem.getAmountStorageUnit())
            .build();
    }

    private ClassID mapClassId(String classId) {
        if (classIdRepository.existsByClassId(classId)) {
            return classIdRepository.getByClassId(classId);
        }
        ClassID id = ClassID.builder().classId(classId).build();
        return classIdRepository.save(id);
    }

    private ItemCategory map(TransientItemCategory type) {
        if (itemTypeRepository.existsByName(type.getName())) {
            return itemTypeRepository.getByName(type.getName());
        }
        ItemCategory category = ItemCategory.builder().name(type.getName()).build();
        return itemCategoryRepository.save(category);
    }

    private List<Sticker> map(List<TransientSticker> stickers) {
        if (stickers == null) {
            return new ArrayList<>();
        }
        List<Sticker> stickerList = new ArrayList<>();
        for (TransientSticker sticker : stickers) {
            if (stickerRepository.existsByName(sticker.getName())) {
                stickerList.add(stickerRepository.getByName(sticker.getName()));
            } else {
                Sticker sticker1 = Sticker.builder().name(sticker.getName()).stickerType(sticker.getStickerType()).build();
                stickerList.add(stickerRepository.save(sticker1));
            }
        }
        return stickerList;
    }

    private ItemName map(String name) {
        if (itemNameRepository.existsByName(name)) {
            return itemNameRepository.getByName(name);
        }
        ItemName name1 = ItemName.builder().name(name).build();
        return itemNameRepository.save(name1);
    }

    private ItemSet map(TransientItemSet itemSet) {
        if (itemSet == null) {
            return null;
        }
        if (itemSetRepository.existsByName(itemSet.getName())) {
            return itemSetRepository.getByName(itemSet.getName());
        }
        ItemSet itemSet1 = ItemSet.builder().name(itemSet.getName()).build();
        return itemSetRepository.save(itemSet1);
    }
}
