package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
    boolean existsByName(String name);

    Sticker getByName(String name);
}
