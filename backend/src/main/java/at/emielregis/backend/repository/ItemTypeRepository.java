package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.*;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.Rarity;
import at.emielregis.backend.data.enums.SpecialItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for {@link ItemType} entities.
 * Provides methods to perform CRUD operations and custom queries related to item types.
 * This repository is responsible for accessing and manipulating data related to items in the database.
 */
public interface ItemTypeRepository extends JpaRepository<ItemType, Long> {

    /**
     * Finds an item type by matching all its properties for equality.
     *
     * @param alreadyStoredSet     The associated {@link ItemSet}, or null.
     * @param alreadyStoredCategory The {@link ItemCategory} of the item.
     * @param alreadyStoredName    The {@link ItemName} of the item.
     * @param exterior             The {@link Exterior} of the item, or null.
     * @param rarity               The {@link Rarity} of the item.
     * @param specialItemType      The {@link SpecialItemType} of the item.
     * @param marketHashName       The market hash name of the item, or null.
     * @return The matching {@link ItemType}, or null if no match is found.
     */
    @Query(
        "SELECT t FROM ItemType t WHERE " +
            "t.itemName = :name AND " +
            "((t.itemSet IS NULL AND :set IS NULL) OR t.itemSet = :set) AND " +
            "t.category = :category AND " +
            "((t.exterior IS NULL AND :exterior IS NULL) OR t.exterior = :exterior) AND " +
            "((t.marketHashName IS NULL AND :markethash IS NULL) OR t.marketHashName = :markethash) AND " +
            "t.rarity = :rarity AND " +
            "t.specialItemType = :specialitemtype"
    )
    ItemType findByEquality(@Param("set") ItemSet alreadyStoredSet,
                            @Param("category") ItemCategory alreadyStoredCategory,
                            @Param("name") ItemName alreadyStoredName,
                            @Param("exterior") Exterior exterior,
                            @Param("rarity") Rarity rarity,
                            @Param("specialitemtype") SpecialItemType specialItemType,
                            @Param("markethash") String marketHashName);

    /**
     * Retrieves all unclassified sticker item types that do not belong to any {@link ItemSet}.
     *
     * @return A list of {@link ItemType} entities representing unclassified stickers.
     */
    @Query("SELECT i FROM ItemType i WHERE i.itemName.name LIKE 'Sticker%' AND i.itemSet IS NULL")
    List<ItemType> getUnclassifiedStickerTypes();

    /**
     * Retrieves all item types associated with a specific {@link ItemSet}.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return A list of {@link ItemType} entities in the given item set.
     */
    @Query("SELECT DISTINCT i FROM ItemType i WHERE i.itemSet = :set")
    List<ItemType> getAllTypesForSet(@Param("set") ItemSet set);

    /**
     * Retrieves all distinct item names associated with a specific {@link ItemSet}.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return A list of {@link ItemName} entities in the given item set.
     */
    @Query("SELECT DISTINCT i.itemName FROM ItemType i WHERE i.itemSet = :set")
    List<ItemName> getAllNamesForSet(@Param("set") ItemSet set);

    /**
     * Retrieves item types based on item name, exterior, and special item type.
     * Parameters can be null to ignore specific criteria.
     *
     * @param itemName        The {@link ItemName} to filter by, or null.
     * @param exterior        The {@link Exterior} to filter by, or null.
     * @param specialItemType The {@link SpecialItemType} to filter by, or null.
     * @return A list of matching {@link ItemType} entities.
     */
    @Query(
        "SELECT t FROM ItemType t WHERE " +
            "(:name IS NULL OR t.itemName = :name) AND " +
            "(:exterior IS NULL OR t.exterior = :exterior) AND " +
            "(:specialitemtype IS NULL OR t.specialItemType = :specialitemtype)"
    )
    List<ItemType> getTypeForItemNameAndParams(@Param("name") ItemName itemName,
                                               @Param("exterior") Exterior exterior,
                                               @Param("specialitemtype") SpecialItemType specialItemType);

    /**
     * Retrieves all item types for a list of item names.
     *
     * @param search A list of {@link ItemName} entities to filter by.
     * @return A list of matching {@link ItemType} entities.
     */
    @Query("SELECT t FROM ItemType t WHERE t.itemName IN :search")
    List<ItemType> getAllTypesForNames(@Param("search") List<ItemName> search);

    /**
     * Retrieves all distinct rarities for a specific item name.
     *
     * @param itemName The {@link ItemName} to filter by.
     * @return A list of {@link Rarity} values associated with the item name.
     */
    @Query("SELECT DISTINCT i.rarity FROM ItemType i WHERE i.itemName = :name")
    List<Rarity> getRarityForItemName(@Param("name") ItemName itemName);

    /**
     * Counts the total number of items with a specific rarity for a list of item types.
     *
     * @param types The list of {@link ItemType} entities to filter by.
     * @param rar   The {@link Rarity} to filter by.
     * @return The total count of items matching the criteria.
     */
    @Query("SELECT SUM(i.amount) FROM ItemCollection i WHERE i.itemType IN :types AND i.itemType.rarity = :rar")
    int countForItemNameAndRarity(@Param("types") List<ItemType> types, @Param("rar") Rarity rar);

    /**
     * Retrieves all charm item names associated with a specific {@link ItemSet}.
     *
     * @param set The {@link ItemSet} to filter by.
     * @return A list of {@link ItemName} entities representing charms in the given set.
     */
    @Query("SELECT DISTINCT t.itemName FROM ItemType t WHERE t.category.name = 'Charm' AND t.itemSet = :set")
    List<ItemName> getCharmItemNamesBySet(@Param("set") ItemSet set);

    /**
     * Retrieves all unclassified charm item names that do not belong to any {@link ItemSet}.
     *
     * @return A list of {@link ItemName} entities representing unclassified charms.
     */
    @Query("SELECT DISTINCT t.itemName FROM ItemType t WHERE t.category.name = 'Charm' AND t.itemSet IS NULL")
    List<ItemName> getUnclassifiedCharmItemNames();
}
