package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.entities.items.Sticker;
import at.emielregis.backend.data.enums.StickerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for {@link Sticker} entities.
 * Provides methods for managing and querying stickers and their applications.
 */
public interface StickerRepository extends JpaRepository<Sticker, Long> {

    /**
     * Counts the total number of stickers applied across all item collections.
     *
     * @return The total count of applied stickers.
     */
    @Query("SELECT sum(size(i.stickers)) FROM ItemCollection i")
    long countDistinctApplied();

    /**
     * Counts the total number of distinct non-applied stickers.
     * These are stickers that exist as standalone items.
     *
     * @return The total count of distinct non-applied stickers.
     */
    @Query("SELECT count(i.name) FROM ItemName i WHERE i.name LIKE 'Sticker%'")
    long countDistinctNonApplied();

    /**
     * Retrieves a sticker by its name and type.
     *
     * @param name The name of the sticker.
     * @param stickerType The {@link StickerType} of the sticker.
     * @return The matching {@link Sticker} entity, or null if not found.
     */
    @Query("SELECT s FROM Sticker s WHERE s.name = :name AND s.stickerType = :type")
    Sticker getByEquality(@Param("name") String name, @Param("type") StickerType stickerType);

    /**
     * Counts the total number of stickers manually applied to items in a specific item set.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return The count of manually applied stickers in the item set.
     */
    @Query(
        "SELECT count(s) FROM ItemCollection i JOIN i.stickers s WHERE " +
            "i.itemType.specialItemType <> at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR AND " +
            "concat('Sticker | ', s.name) IN " +
            "(SELECT DISTINCT s1.itemType.itemName.name FROM ItemCollection s1 WHERE s1.itemType.itemSet = :set)"
    )
    long countTotalManuallyAppliedForSet(@Param("set") ItemSet set);

    /**
     * Counts the total number of stickers applied to souvenir items in a specific item set.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return The count of souvenir-applied stickers in the item set.
     */
    @Query(
        "SELECT count(s) FROM ItemCollection i JOIN i.stickers s WHERE " +
            "i.itemType.specialItemType = at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR AND " +
            "concat('Sticker | ', s.name) IN " +
            "(SELECT DISTINCT s1.itemType.itemName.name FROM ItemCollection s1 WHERE s1.itemType.itemSet = :set)"
    )
    long countTotalSouvenirAppliedForSet(@Param("set") ItemSet set);

    /**
     * Counts the total number of times a specific sticker has been manually applied to items.
     *
     * @param name The name of the sticker.
     * @return The count of manually applied instances of the sticker.
     */
    @Query(
        "SELECT count(s) FROM ItemCollection i JOIN i.stickers s WHERE " +
            "i.itemType.specialItemType <> at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR AND s.name = :name"
    )
    Long countTotalManuallyAppliedForItemName(@Param("name") String name);

    /**
     * Counts the total number of times a specific sticker has been applied to souvenir items.
     *
     * @param name The name of the sticker.
     * @return The count of souvenir-applied instances of the sticker.
     */
    @Query(
        "SELECT count(s) FROM ItemCollection i JOIN i.stickers s WHERE " +
            "i.itemType.specialItemType = at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR AND s.name = :name"
    )
    Long countTotalSouvenirAppliedForItemName(@Param("name") String name);
}
