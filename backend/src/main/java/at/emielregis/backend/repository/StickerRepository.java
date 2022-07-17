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
    long countDistinctApplied();

    @Query(
        "select count (i.name) from ItemName i where i.name like 'Sticker%'"
    )
    long countDistinctNonApplied();

    @Query(
        "select count (s) from Item i join i.stickers s where i.souvenir = false AND concat('Sticker | ', s.name) in (select distinct s1.name.name from Item s1 where s1.itemSet = :set)"
    )
    long countTotalManuallyAppliedForSet(@Param("set") ItemSet set);

    @Query(
        "select count (s) from Item i join i.stickers s where i.souvenir = true AND concat('Sticker | ', s.name) in (select distinct s1.name.name from Item s1 where s1.itemSet = :set)"
    )
    long countTotalSouvenirAppliedForSet(@Param("set") ItemSet set);

    @Query(
        "select count (s) from Item i join i.stickers s where i.souvenir = false AND s.name = :name"
    )
    Long countTotalManuallyAppliedForItemName(@Param("name") String name);

    @Query(
        "select count (s) from Item i join i.stickers s where i.souvenir = true AND s.name = :name"
    )
    Long countTotalSouvenirAppliedForItemName(@Param("name") String name);
}
