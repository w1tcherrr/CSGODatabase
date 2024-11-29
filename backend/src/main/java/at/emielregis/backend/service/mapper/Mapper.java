package at.emielregis.backend.service.mapper;

import at.emielregis.backend.data.entities.items.*;
import at.emielregis.backend.repository.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mapper component that converts transient items to persistent entities,
 * ensuring that sub-entities like ItemName, Stickers, and Charms are stored
 * only once in the database. This helps maintain data integrity and reduces
 * redundancy by reusing existing database entries.
 */
@Component
public class Mapper {

    private final ItemTypeRepository itemTypeRepository;
    private final StickerRepository stickerRepository;
    private final CharmRepository charmRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemNameRepository itemNameRepository;
    private final ItemSetRepository itemSetRepository;

    /**
     * Constructs a new Mapper with the required repositories.
     *
     * @param itemTypeRepository     Repository for ItemType entities.
     * @param stickerRepository      Repository for Sticker entities.
     * @param charmRepository        Repository for Charm entities.
     * @param itemCategoryRepository Repository for ItemCategory entities.
     * @param itemNameRepository     Repository for ItemName entities.
     * @param itemSetRepository      Repository for ItemSet entities.
     */
    public Mapper(ItemTypeRepository itemTypeRepository,
                  StickerRepository stickerRepository,
                  CharmRepository charmRepository,
                  ItemCategoryRepository itemCategoryRepository,
                  ItemNameRepository itemNameRepository,
                  ItemSetRepository itemSetRepository) {
        this.itemTypeRepository = itemTypeRepository;
        this.stickerRepository = stickerRepository;
        this.charmRepository = charmRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.itemNameRepository = itemNameRepository;
        this.itemSetRepository = itemSetRepository;
    }

    /**
     * Maps a transient {@link ItemCollection} to a persistent entity,
     * ensuring that sub-entities are stored only once in the database.
     * This method is synchronized to prevent race conditions when accessing
     * or modifying shared resources.
     *
     * @param transientItem The transient {@link ItemCollection} to be mapped.
     * @return The persistent {@link ItemCollection} entity.
     */
    public synchronized ItemCollection convertToNonTransient(ItemCollection transientItem) {
        return ItemCollection.builder()
            .amount(transientItem.getAmount())
            .nameTag(transientItem.getNameTag())
            .itemType(mapItemType(transientItem.getItemType()))
            .stickers(mapStickers(transientItem.getStickers()))
            .charm(mapCharm(transientItem.getCharm()))
            .build();
    }

    /**
     * Maps a transient {@link ItemType} to a persistent entity,
     * ensuring sub-entities like {@link ItemSet}, {@link ItemCategory},
     * and {@link ItemName} are stored only once.
     *
     * @param itemType The transient {@link ItemType} to be mapped.
     * @return The persistent {@link ItemType} entity.
     */
    private ItemType mapItemType(ItemType itemType) {
        ItemSet storedSet = null;
        ItemCategory storedCategory = null;
        ItemName storedName = null;

        boolean isSetStored = false;
        boolean isCategoryStored = false;
        boolean isNameStored = false;

        // Map ItemSet
        if (itemType.getItemSet() != null) {
            storedSet = itemSetRepository.findByName(itemType.getItemSet().getName());
            if (storedSet == null) {
                storedSet = itemSetRepository.save(itemType.getItemSet());
            } else {
                isSetStored = true;
            }
        } else {
            isSetStored = true;
        }

        // Map ItemCategory
        if (itemType.getCategory() != null) {
            storedCategory = itemCategoryRepository.findByName(itemType.getCategory().getName());
            if (storedCategory == null) {
                storedCategory = itemCategoryRepository.save(itemType.getCategory());
            } else {
                isCategoryStored = true;
            }
        } else {
            isCategoryStored = true;
        }

        // Map ItemName
        if (itemType.getItemName() != null) {
            storedName = itemNameRepository.findByName(itemType.getItemName().getName());
            if (storedName == null) {
                storedName = itemNameRepository.save(itemType.getItemName());
            } else {
                isNameStored = true;
            }
        } else {
            isNameStored = true;
        }

        // Check if the ItemType already exists to prevent duplicates
        if (isSetStored && isCategoryStored && isNameStored) {
            ItemType storedType = itemTypeRepository.findByEquality(
                storedSet,
                storedCategory,
                storedName,
                itemType.getExterior(),
                itemType.getRarity(),
                itemType.getSpecialItemType(),
                itemType.getMarketHashName()
            );
            if (storedType != null) {
                return storedType;
            }
        }

        // Save the new ItemType if it doesn't exist
        return itemTypeRepository.save(
            ItemType.builder()
                .itemName(storedName)
                .category(storedCategory)
                .itemSet(storedSet)
                .exterior(itemType.getExterior())
                .rarity(itemType.getRarity())
                .specialItemType(itemType.getSpecialItemType())
                .marketHashName(itemType.getMarketHashName())
                .build()
        );
    }

    /**
     * Maps a list of transient {@link Sticker} entities to persistent entities,
     * ensuring that each sticker is stored only once in the database.
     *
     * @param stickers The list of transient {@link Sticker} entities.
     * @return A list of persistent {@link Sticker} entities.
     */
    private List<Sticker> mapStickers(List<Sticker> stickers) {
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

    /**
     * Maps a transient {@link Charm} entity to a persistent entity,
     * ensuring it's stored only once in the database.
     *
     * @param charm The transient {@link Charm} to be mapped.
     * @return The persistent {@link Charm} entity, or null if the input is null.
     */
    private Charm mapCharm(Charm charm) {
        if (charm == null) {
            return null;
        }
        // Attempt to find an existing charm in the database by name
        Charm storedCharm = charmRepository.findByName(charm.getName());
        // Save the new charm to the database if it doesn't exist
        return Objects.requireNonNullElseGet(storedCharm, () -> charmRepository.save(
            Charm.builder()
                .name(charm.getName())
                .build()
        ));
    }
}
