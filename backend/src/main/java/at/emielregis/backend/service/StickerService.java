package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.repository.ItemNameRepository;
import at.emielregis.backend.repository.StickerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public record StickerService(StickerRepository stickerRepository, ItemNameRepository itemNameRepository) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public long countTypes() {
        LOGGER.info("StickerService#count()");
        return stickerRepository.count();
    }

    public long countDistinctNonApplied() {
        LOGGER.info("StickerService#countDistinctNonApplied()");
        return stickerRepository.countDistinctNonApplied();
    }

    public long countDistinctApplied() {
        LOGGER.info("StickerService#countDistinctApplied()");
        return stickerRepository.countDistinctApplied();
    }

    public long countTotalManuallyAppliedForSet(ItemSet set) {
        LOGGER.info("StickerService#countTotalManuallyAppliedForSet(" + set.toString() + ")");
        return stickerRepository.countTotalManuallyAppliedForSet(set);
    }

    public long countTotalSouvenirAppliedForSet(ItemSet set) {
        LOGGER.info("StickerService#countTotalSouvenirAppliedForSet(" + set.toString() + ")");
        return stickerRepository.countTotalSouvenirAppliedForSet(set);
    }

    public long countTotalManuallyAppliedForItemName(String itemNameName) {
        LOGGER.info("StickerService#countTotalManuallyAppliedForItemName(" + itemNameName + ")");
        return stickerRepository.countTotalManuallyAppliedForItemName(itemNameName.substring(10));
    }

    public long countTotalSouvenirAppliedForItemName(String itemNameName) {
        LOGGER.info("StickerService#countTotalSouvenirAppliedForItemName(" + itemNameName + ")");
        Long amount = stickerRepository.countTotalSouvenirAppliedForItemName(itemNameName.substring(10));
        if (amount == null) {
            return 0;
        }
        return amount;
    }
}
