package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.repository.ItemNameRepository;
import at.emielregis.backend.repository.StickerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * Service class for managing sticker-related operations.
 * Provides methods to retrieve statistics and perform queries on stickers.
 */
@Component
public record StickerService(StickerRepository stickerRepository, ItemNameRepository itemNameRepository) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Counts the total number of sticker types in the database.
     *
     * @return The count of sticker types.
     */
    public long countTypes() {
        LOGGER.info("StickerService#countTypes()");
        return stickerRepository.count();
    }

    /**
     * Counts the total number of distinct non-applied stickers.
     *
     * @return The count of distinct non-applied stickers.
     */
    public long countDistinctNonApplied() {
        LOGGER.info("StickerService#countDistinctNonApplied()");
        return stickerRepository.countDistinctNonApplied();
    }

    /**
     * Counts the total number of distinct applied stickers.
     *
     * @return The count of distinct applied stickers.
     */
    public long countDistinctApplied() {
        LOGGER.info("StickerService#countDistinctApplied()");
        return stickerRepository.countDistinctApplied();
    }

    /**
     * Counts the total number of manually applied stickers for a specific item set.
     *
     * @param set The {@link ItemSet}.
     * @return The count of manually applied stickers.
     */
    public long countTotalManuallyAppliedForSet(ItemSet set) {
        LOGGER.info("StickerService#countTotalManuallyAppliedForSet({})", set.getName());
        return stickerRepository.countTotalManuallyAppliedForSet(set);
    }

    /**
     * Counts the total number of Souvenir stickers applied for a specific item set.
     *
     * @param set The {@link ItemSet}.
     * @return The count of Souvenir stickers applied.
     */
    public long countTotalSouvenirAppliedForSet(ItemSet set) {
        LOGGER.info("StickerService#countTotalSouvenirAppliedForSet({})", set.getName());
        return stickerRepository.countTotalSouvenirAppliedForSet(set);
    }

    /**
     * Counts the total number of manually applied stickers for a specific item name.
     *
     * @param itemNameName The name of the item.
     * @return The count of manually applied stickers.
     */
    public long countTotalManuallyAppliedForItemName(String itemNameName) {
        LOGGER.info("StickerService#countTotalManuallyAppliedForItemName({})", itemNameName);
        return stickerRepository.countTotalManuallyAppliedForItemName(itemNameName.substring(10));
    }

    /**
     * Counts the total number of Souvenir stickers applied for a specific item name.
     *
     * @param itemNameName The name of the item.
     * @return The count of Souvenir stickers applied.
     */
    public long countTotalSouvenirAppliedForItemName(String itemNameName) {
        LOGGER.info("StickerService#countTotalSouvenirAppliedForItemName({})", itemNameName);
        Long amount = stickerRepository.countTotalSouvenirAppliedForItemName(itemNameName.substring(10));
        return amount == null ? 0 : amount;
    }
}
