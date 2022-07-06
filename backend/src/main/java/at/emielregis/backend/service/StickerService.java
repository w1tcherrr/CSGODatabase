package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.Sticker;
import at.emielregis.backend.repository.StickerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public record StickerService(StickerRepository stickerRepository) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public long appliedStickerCount() {
        LOGGER.info("StickerService#appliedStickerCount()");
        return stickerRepository.appliedStickerCount();
    }

    public long count() {
        LOGGER.info("StickerService#count()");
        return stickerRepository.count();
    }

    public long countNonApplied() {
        LOGGER.info("StickerService#countNonApplied()");
        return stickerRepository.countNonApplied();
    }
}
