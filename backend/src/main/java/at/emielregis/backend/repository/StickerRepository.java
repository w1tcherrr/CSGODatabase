package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.entities.items.Sticker;
import at.emielregis.backend.data.enums.StickerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
    @Query(
        "select sum(i.stickers.size) from ItemCollection i"
    )
    long countDistinctApplied();

    @Query(
        "select count (i.name) from ItemName i where i.name like 'Sticker%'"
    )
    long countDistinctNonApplied();

    @Query(
        "select s from Sticker s where s.name = :name and s.stickerType = :type"
    )
    Sticker getByEquality(@Param("name") String name, @Param("type") StickerType stickerType);

    @Query(
        "select count (s) from ItemCollection i join i.stickers s where i.itemType.specialItemType <> at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR AND concat('Sticker | ', s.name) in (select distinct s1.itemType.itemName.name from ItemCollection s1 where s1.itemType.itemSet = :set)"
    )
    long countTotalManuallyAppliedForSet(@Param("set") ItemSet set);

    @Query(
        "select count (s) from ItemCollection i join i.stickers s where i.itemType.specialItemType = at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR AND concat('Sticker | ', s.name) in (select distinct s1.itemType.itemName.name from ItemCollection s1 where s1.itemType.itemSet = :set)"
    )
    long countTotalSouvenirAppliedForSet(@Param("set") ItemSet set);

    @Query(
        "select count (s) from ItemCollection i join i.stickers s where i.itemType.specialItemType <> at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR AND s.name = :name"
    )
    Long countTotalManuallyAppliedForItemName(@Param("name") String name);

    @Query(
        "select count (s) from ItemCollection i join i.stickers s where i.itemType.specialItemType = at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR AND s.name = :name"
    )
    Long countTotalSouvenirAppliedForItemName(@Param("name") String name);
}
