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
 */
public interface ItemTypeRepository extends JpaRepository<ItemType, Long> {

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

    @Query("SELECT i FROM ItemType i WHERE i.itemName.name LIKE 'Sticker%' AND i.itemSet IS NULL")
    List<ItemType> getUnclassifiedStickerTypes();

    @Query("SELECT DISTINCT i FROM ItemType i WHERE i.itemSet = :set")
    List<ItemType> getAllTypesForSet(@Param("set") ItemSet set);

    @Query("SELECT DISTINCT i.itemName FROM ItemType i WHERE i.itemSet = :set")
    List<ItemName> getAllNamesForSet(@Param("set") ItemSet set);

    @Query(
        "SELECT t FROM ItemType t WHERE " +
            "(:name IS NULL OR t.itemName = :name) AND " +
            "(:exterior IS NULL OR t.exterior = :exterior) AND " +
            "(:specialitemtype IS NULL OR t.specialItemType = :specialitemtype)"
    )
    List<ItemType> getTypeForItemNameAndParams(@Param("name") ItemName itemName,
                                               @Param("exterior") Exterior exterior,
                                               @Param("specialitemtype") SpecialItemType specialItemType);

    @Query("SELECT t FROM ItemType t WHERE t.itemName IN :search")
    List<ItemType> getAllTypesForNames(@Param("search") List<ItemName> search);

    @Query("SELECT DISTINCT i.rarity FROM ItemType i WHERE i.itemName = :name")
    List<Rarity> getRarityForItemName(@Param("name") ItemName itemName);

    @Query("SELECT SUM(i.amount) FROM ItemCollection i WHERE i.itemType IN :types AND i.itemType.rarity = :rar")
    int countForItemNameAndRarity(@Param("types") List<ItemType> types, @Param("rar") Rarity rar);

    @Query("SELECT DISTINCT t.itemName FROM ItemType t WHERE t.category.name = 'Charm' AND t.itemSet = :set")
    List<ItemName> getCharmItemNamesBySet(@Param("set") ItemSet set);

    @Query("SELECT DISTINCT t.itemName FROM ItemType t WHERE t.category.name = 'Charm' AND t.itemSet IS NULL")
    List<ItemName> getUnclassifiedCharmItemNames();

}
