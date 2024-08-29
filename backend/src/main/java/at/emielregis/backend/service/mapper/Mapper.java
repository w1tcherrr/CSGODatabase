package at.emielregis.backend.service.mapper;

import at.emielregis.backend.data.entities.items.*;
import at.emielregis.backend.repository.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public record Mapper(ItemTypeRepository itemTypeRepository,
                     StickerRepository stickerRepository,
                     ItemCategoryRepository itemCategoryRepository,
                     ItemNameRepository itemNameRepository,
                     ItemSetRepository itemSetRepository) {

    /**
     * Maps a transient item to an entity item.
     * Stores the sub-entities into the database already, such as ItemName, Stickers, etc.
     *
     * @param transientItem The item to be mapped.
     * @return The database entity (not stored in the database yet!).
     */
    public synchronized ItemCollection convertToNonTransient(ItemCollection transientItem) {
        return ItemCollection.builder()
            .amount(transientItem.getAmount())
            .nameTag(transientItem.getNameTag())
            .itemType(map(transientItem.getItemType()))
            .stickers(map(transientItem.getStickers()))
            .build();
    }

    private ItemType map(ItemType itemType) {
        ItemSet alreadyStoredSet = null;
        ItemCategory alreadyStoredCategory = null;
        ItemName alreadyStoredName = null;

        boolean setSet = false;
        boolean categorySet = false;
        boolean nameSet = false;

        if (itemType.getItemSet() != null) {
            alreadyStoredSet = itemSetRepository.findByName(itemType.getItemSet().getName());
            if (alreadyStoredSet == null) {
                alreadyStoredSet = itemSetRepository.save(itemType.getItemSet());
            } else {
                setSet = true;
            }
        } else {
            setSet = true;
        }

        if (itemType.getCategory() != null) {
            alreadyStoredCategory = itemCategoryRepository.findByName(itemType.getCategory().getName());
            if (alreadyStoredCategory == null) {
                alreadyStoredCategory = itemCategoryRepository.save(itemType.getCategory());
            } else {
                categorySet = true;
            }
        } else {
            categorySet = true;
        }

        if (itemType.getItemName() != null) {
            alreadyStoredName = itemNameRepository.findByName(itemType.getItemName().getName());
            if (alreadyStoredName == null) {
                alreadyStoredName = itemNameRepository.save(itemType.getItemName());
            } else {
                nameSet = true;
            }
        } else {
            nameSet = true;
        }

        if (setSet && categorySet && nameSet) {
            ItemType alreadyStoredType = itemTypeRepository.findByEquality(alreadyStoredSet, alreadyStoredCategory, alreadyStoredName,
                itemType.getExterior(), itemType.getRarity(), itemType.getSpecialItemType(), itemType.getMarketHashName());
            if (alreadyStoredType != null) {
                return alreadyStoredType;
            }
        }

        return itemTypeRepository.save(
            ItemType.builder()
                .itemName(alreadyStoredName)
                .category(alreadyStoredCategory)
                .itemSet(alreadyStoredSet)
                .exterior(itemType.getExterior())
                .rarity(itemType.getRarity())
                .specialItemType(itemType.getSpecialItemType())
                .marketHashName(itemType.getMarketHashName())
                .build()
        );
    }

    private List<Sticker> map(List<Sticker> stickers) {
        List<Sticker> mappedStickers = new ArrayList<>();
        if (stickers == null) {
            return mappedStickers;
        }
        for (Sticker sticker : stickers) {
            Sticker storedSticker = stickerRepository.getByEquality(sticker.getName(), sticker.getStickerType());
            mappedStickers.add(Objects.requireNonNullElseGet(storedSticker,
                () -> stickerRepository.save(
                    Sticker.builder()
                        .name(sticker.getName())
                        .stickerType(sticker.getStickerType())
                        .build()
                )));
        }
        return mappedStickers;
    }
}
