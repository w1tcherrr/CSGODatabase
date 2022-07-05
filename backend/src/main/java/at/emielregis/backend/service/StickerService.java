package at.emielregis.backend.service;

import at.emielregis.backend.repository.StickerRepository;
import org.springframework.stereotype.Component;

@Component
public record StickerService(StickerRepository stickerRepository) {
    public long appliedStickerCount() {
        return stickerRepository.count();
    }

    public long uniqueStickerCount() {
        return stickerRepository.uniqueCount();
    }
}
