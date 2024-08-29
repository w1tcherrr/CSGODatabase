package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemCategory;
import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.entities.items.ItemType;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.data.enums.Rarity;
import at.emielregis.backend.data.enums.SpecialItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemTypeRepository extends JpaRepository<ItemType, Long> {
    @Query(
        "Select t from ItemType t where " +
            "t.itemName = :name and " +
            "((t.itemSet IS NULL and :set IS NULL) OR t.itemSet = :set) and " +
            "t.category = :category and " +
            "((t.exterior IS NULL and :exterior is NULL) OR t.exterior = :exterior) and " +
            "((t.marketHashName IS NULL and :markethash is NULL) or t.marketHashName = :markethash) and " +
            "t.rarity = :rarity and " +
            "t.specialItemType = :specialitemtype"
    )
    ItemType findByEquality(@Param("set") ItemSet alreadyStoredSet,
                            @Param("category") ItemCategory alreadyStoredCategory,
                            @Param("name") ItemName alreadyStoredName,
                            @Param("exterior") Exterior exterior,
                            @Param("rarity") Rarity rarity,
                            @Param("specialitemtype") SpecialItemType specialItemType,
                            @Param("markethash") String marketHashName);

    @Query(
        "Select i from ItemType i where i.itemName.name like 'Sticker%' and i.itemSet IS NULL"
    )
    List<ItemType> getUnclassifiedStickerTypes();

    @Query(
        "SELECT distinct i from ItemType i where i.itemSet = :set"
    )
    List<ItemType> getAllTypesForSet(@Param("set") ItemSet set);

    @Query(
        "SELECT distinct i.itemName from ItemType i where i.itemSet = :set"
    )
    List<ItemName> getAllNamesForSet(@Param("set") ItemSet set);

    @Query(
        "Select t from ItemType t where " +
            "(:name IS NULL OR t.itemName = :name) and " +
            "(:exterior IS NULL OR t.exterior = :exterior) and " +
            "(:specialitemtype IS NULL OR t.specialItemType = :specialitemtype)"
    )
    List<ItemType> getTypeForItemNameAndParams(@Param("name") ItemName itemName,
                                               @Param("exterior") Exterior exterior,
                                               @Param("specialitemtype") SpecialItemType specialItemType);

    @Query(
        "Select t from ItemType t where t.itemName in :search"
    )
    List<ItemType> getAllTypesForNames(@Param("search") List<ItemName> search);

    @Query(
        "Select distinct i.rarity from ItemType i where i.itemName = :name"
    )
    List<Rarity> getRarityForItemName(@Param("name") ItemName itemName);

    @Query(
        "Select sum(i.amount) from ItemCollection i where i.itemType in :types and i.itemType.rarity = :rar"
    )
    int countForItemNameAndRarity(@Param("types") List<ItemType> types, @Param("rar") Rarity rar);
}
