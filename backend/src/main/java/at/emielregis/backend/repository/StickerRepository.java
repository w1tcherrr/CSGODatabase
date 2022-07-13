package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.data.entities.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
    boolean existsByName(String name);

    Sticker getByName(String name);

    @Query(
        "select sum(i.stickers.size) from Item i"
    )
    long appliedStickerCount();

    @Query(
        "select count (i.name) from ItemName i where i.name like 'Sticker%'"
    )
    long countNonApplied();

    @Query(
        "select count (s) from Item i join i.stickers s where concat('Sticker | ', s.name) in (select distinct s1.name.name from Item s1 where s1.itemSet = :set)"
    )
    long countAppliedForSet(@Param("set") ItemSet set);

    @Query(
        "select count (s) from Item i join i.stickers s where s.name = :name"
    )
    long countAppliedForItemName(@Param("name") String name);
}
