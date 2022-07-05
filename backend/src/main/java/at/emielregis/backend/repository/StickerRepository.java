package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
    boolean existsByName(String name);

    Sticker getByName(String name);

    @Query(
        "select count(distinct s.name) from Sticker s"
    )
    long uniqueCount();
}
