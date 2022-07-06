package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
    boolean existsByName(String name);

    Sticker getByName(String name);

    @Query(
        "select count(s) from CSGOInventory inv join inv.items i join i.stickers s"
    )
    long appliedStickerCount();

    @Query(
        "select count (i.name) from ItemName i where i.name like 'Sticker%'"
    )
    long countNonApplied();
}
